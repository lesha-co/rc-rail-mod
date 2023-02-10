package co.lesha.minecraftmods.rc_rail_mod;


import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.RailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public record RailWalker(World world, BlockPos pos) {
  @Override
  public String toString() {
    return "Walker@[" + pos.getX() + "," + pos.getZ() + "] " + this.getShape();
  }

  public static boolean isRail(BlockState state) {
    return state.isIn(BlockTags.RAILS);
  }

  public boolean isRailAtPos(BlockPos pos) {
    return isRail(world.getBlockState(pos));
  }

  public boolean canMakeCurves() {
    return this.getState().getBlock() instanceof RailBlock;
  }

  public RailShape getShape() {
    BlockState state = this.getState();
    return state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty());
  }

  private static boolean directionIsHorizontal(Direction d) {
    return d.getAxis() != Direction.Axis.Y;
  }

  public BlockState getState() {
    return world.getBlockState(pos);
  }

  public RailWalker(World world, BlockPos pos) {
    this.world = world;
    this.pos = pos;
    assert isRail(getState());
  }

  private RailWalker createFromPos(BlockPos pos) {
    return new RailWalker(world, pos);
  }

  /**
   * get set of sides on which this block has connections.
   * Example: South-West block can be entered <strong>FROM SOUTH</strong>, neighbouring block must have
   * <strong>NORTH</strong> exit. Traveling minecart will go NORTH when entering, go <strong>WEST</strong> when exiting
   *
   * @return
   */
  public Set<Direction> getFacings() {
    return switch (this.getShape()) {
      case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH ->
        new HashSet<>(Arrays.asList(Direction.SOUTH, Direction.NORTH));
      case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> new HashSet<>(Arrays.asList(Direction.EAST, Direction.WEST));
      case SOUTH_EAST -> new HashSet<>(Arrays.asList(Direction.SOUTH, Direction.EAST));
      case SOUTH_WEST -> new HashSet<>(Arrays.asList(Direction.SOUTH, Direction.WEST));
      case NORTH_WEST -> new HashSet<>(Arrays.asList(Direction.NORTH, Direction.WEST));
      case NORTH_EAST -> new HashSet<>(Arrays.asList(Direction.NORTH, Direction.EAST));
    };
  }

  /**
   * Find the direction a minecart will be traveling in when exiting this block, if entering from {@code enteringFrom}
   *
   * @param enteringFrom facing of a rail block from which the minecart enters it, e.g. minecart traveling NORTH will
   *                     enter from SOUTH facing
   * @return
   * @link <a href="https://minecraft.fandom.com/wiki/Rail#Minecart_behavior">Minecart behavior</a>
   */
  public Direction findExit(Direction enteringFrom) {
    var facings = getFacings();
    if (facings.contains(enteringFrom)) {
      // Minecart enters from known facing, will exit from another one
      var other = facings.stream().filter(d -> d != enteringFrom).findFirst();
      return other.orElseThrow();
    }
    if (facings.contains(enteringFrom.getOpposite())) {
      // see: https://minecraft.fandom.com/wiki/Rail?file=T-intersection-south-west.png#Minecart_behavior
      // Minecart enters curved rail from unknown facing (it has opposite facing, but doesn't have current),
      // will exit from opposite side
      return enteringFrom.getOpposite();
    }
    // south-east rule
    if (facings.contains(Direction.SOUTH)) {
      return Direction.SOUTH;
    }

    return Direction.EAST;
  }

  public Optional<RailWalker> findNextRailInDirection(Direction direction) {
    assert directionIsHorizontal(direction);
    var nextBlockPos = this.pos.offset(direction);
    var ascendingTowards = this.ascendingTowards();
    // checking whether we need to search at upper level
    if (ascendingTowards.isPresent() && ascendingTowards.get() == direction) {
      // we are standing on ascending rail,
      // looking from its "upper" side, next block MUST be higher
      var nextBlockPosUp = nextBlockPos.up();
      return isRailAtPos(nextBlockPosUp)
        ? Optional.of(createFromPos(nextBlockPosUp))
        : Optional.empty();
    }

    // checking current level
    if (isRailAtPos(nextBlockPos)) {
      return Optional.of(createFromPos(nextBlockPos));
    }

    // checking lower level
    var nextBlockPosDown = nextBlockPos.down();
    if (isRailAtPos(nextBlockPosDown)) {
      var otherAscension = createFromPos(nextBlockPosDown).ascendingTowards();
      if(otherAscension.isPresent() && otherAscension.get() == direction.getOpposite()) {
        return Optional.of(createFromPos(nextBlockPosDown));
      }

    }
    return Optional.empty();

  }

  private Set<Direction> findAllNeighbourDirections() {
    Set<Direction> directions = new HashSet<>();
    for (Direction d : Direction.Type.HORIZONTAL) {
      findNextRailInDirection(d).ifPresent(x -> directions.add(d));
    }
    return directions;
  }

  private Optional<RailWalker> getConnectionAt(Direction direction) {
    RailRemoteControlMod.LOGGER.info("checking " + this + " on " + direction.toString());
    var maybeOtherRail = findNextRailInDirection(direction);
    if (maybeOtherRail.isEmpty()) {
      RailRemoteControlMod.LOGGER.info("no rail");
      return Optional.empty();
    }

    var otherRail = maybeOtherRail.get();
    if (!otherRail.getFacings().contains(direction.getOpposite())) {
      RailRemoteControlMod.LOGGER.info("rail exists, but not connected to this");
      return Optional.empty();
    }
    RailRemoteControlMod.LOGGER.info("rail[" + this + "] is connected to " + otherRail);
    return Optional.of(otherRail);

  }

  public Set<RailWalker> getConnections() {
    Set<RailWalker> rwSet = new HashSet<>();
    RailRemoteControlMod.LOGGER.info("checking connectivity at " + this);
    for (var facing : getFacings()) {
      getConnectionAt(facing).ifPresent(rwSet::add);
    }
    return rwSet;
  }

  /**
   * Suitable direction for this rail has other rail that's either:
   * - connected to this rail
   * - connected to one or zero other rails
   * and
   * - does not ascend except
   * - away from junction
   * - towards junction from one block below
   * - is not "straight-only" block (except if facing junction)
   *
   * @return
   */
  public Set<Direction> findAllSuitableNeighbourDirections() {
    RailRemoteControlMod.LOGGER.info("Finding suitable directions for " + this);
    var allDirections = findAllNeighbourDirections();

    var suitable = allDirections.stream()
      .filter(d -> {

        RailRemoteControlMod.LOGGER.info("  Checking " + d.toString() + " of " + this);
        var nextRailInDirection = findNextRailInDirection(d).orElseThrow();
        var nextRailAscension = nextRailInDirection.ascendingTowards();
        if (nextRailAscension.isPresent()) {
          if (nextRailInDirection.pos.getY() == this.pos.getY()) {
            if (nextRailAscension.get() != d) return false;
          } else {
            if (nextRailAscension.get() != d.getOpposite()) return false;
          }
        }
        if (!nextRailInDirection.canMakeCurves() && !nextRailInDirection.hasFacing(d.getOpposite())) {
          return false;
        }
        if (nextRailInDirection.hasFacing(d.getOpposite())) {
          return true;
        }
        if (nextRailInDirection.getConnections().size() < 2) {
          return true;
        }
        return false;
      })
      .collect(Collectors.toSet());
    RailRemoteControlMod.LOGGER.info("Found " + suitable.size() + " suitable directions for " + this);
    return suitable;
  }

  private boolean hasFacing(Direction d) {
    return getFacings().contains(d);
  }

  /**
   * <a href="https://minecraft.fandom.com/wiki/Rail#Block_states">See Minecraft wiki</a>
   *
   * @return the direction towards which the rail ascends
   */
  private Optional<Direction> ascendingTowards() {
    return switch (this.getShape()) {
      case ASCENDING_EAST -> Optional.of(Direction.EAST);
      case ASCENDING_WEST -> Optional.of(Direction.WEST);
      case ASCENDING_NORTH -> Optional.of(Direction.NORTH);
      case ASCENDING_SOUTH -> Optional.of(Direction.SOUTH);
      default -> Optional.empty();
    };
  }


  /**
   * Finds next block that satisfies junction condition(more than two neighbours)
   *
   * @param dir
   * @param nBlocksToScanFor scan for this amount of blocks
   * @return
   */
  public Optional<RailJunction> getNextJunction(Direction dir, int nBlocksToScanFor) {
    RailRemoteControlMod.LOGGER.info("walking " + dir.toString() + " at " + this);
    assert directionIsHorizontal(dir);
    if (nBlocksToScanFor <= 0) {
      return Optional.empty();
    }

    var next = this.findNextRailInDirection(dir);
    if (next.isEmpty()) { // если next пустой
      return Optional.empty(); //вернуть пустое значение
    }
    if (next.get().isJunctionCondition()) { // get() тут вернет инстанс класса RailWalker
      return Optional.of(new RailJunction(next.get(), dir.getOpposite())); // Optional.of() создает optional со значением
    }

    // Cart is traveling in direction {dir} so it enters block from its opposite facing
    Direction d = next.get().findExit(dir.getOpposite());
    return next.get().getNextJunction(d, nBlocksToScanFor - 1);
  }


  private boolean isJunctionCondition() {
    return this.findAllSuitableNeighbourDirections().size() > 2
      && this.canMakeCurves()
      && !this.getShape().isAscending();
  }

  /**
   * Method to change this rail's shape in order to change one of its sides
   * (north-south -> north-east) in response to nearby junction switch (in this case, junction to the east requested )
   * - if both sides are connected, the request is ignored
   * - if rail can't make curves, the request is ignored
   * - if both sides are disconnected, the rail is replaced by straight rail going towards the attractor
   * - if one side is disconnected, the rail is replaced by one that's going from connected side to the junction
   *
   * @param attractionFacing the requested direction
   */
  public void attract(Direction attractionFacing) {
    if (!this.canMakeCurves() || this.ascendingTowards().isPresent()) {
      return;
    }

    var disconnectedFacings = getFacings().stream().filter(face -> getConnectionAt(face).isEmpty()).toList();
    var connectedFacings = getFacings();
    connectedFacings.removeAll(disconnectedFacings);
    var connectedList = connectedFacings.stream().toList();
    Optional<Direction> otherFacing = switch (disconnectedFacings.size()) {
      default -> Optional.empty();
      case 1 -> Optional.of(connectedList.get(0));
      case 2 -> Optional.of(attractionFacing.getOpposite());
    };
    if (otherFacing.isPresent()) {
      var shape = RailRemoteControlMod.constructShape(attractionFacing, otherFacing.get());
      shape.ifPresent(this::replaceShape);
    }

  }

  public void replaceShape(RailShape shape) {
    this.world.setBlockState(this.pos, getState().with(Properties.RAIL_SHAPE, shape));
  }
}
