package co.lesha.minecraftmods.rc_rail_mod;

import net.minecraft.block.enums.RailShape;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

import java.util.*;

public class RailJunction {
  public RailWalker rw;
  public Direction fixedPart;

  public RailJunction(RailWalker rw, Direction fixedPart) {
    this.rw = rw;
    this.fixedPart = fixedPart;
  }

  public Optional<RailShape> constructShape(Direction d1, Direction d2) {
    if (d1 == d2 || d1 == Direction.UP || d1 == Direction.DOWN || d2 == Direction.UP || d2 == Direction.DOWN) {
      return Optional.empty();
    }
    switch (d1) {
      case NORTH -> {
        switch (d2) {
          case SOUTH -> {
            return Optional.of(RailShape.NORTH_SOUTH);
          }
          case WEST -> {
            return Optional.of(RailShape.NORTH_WEST);
          }
          case EAST -> {
            return Optional.of(RailShape.NORTH_EAST);
          }
        }
      }
      case SOUTH -> {
        switch (d2) {
          case NORTH -> {
            return Optional.of(RailShape.NORTH_SOUTH);
          }
          case WEST -> {
            return Optional.of(RailShape.SOUTH_WEST);
          }
          case EAST -> {
            return Optional.of(RailShape.SOUTH_EAST);
          }
        }
      }
      case WEST -> {
        switch (d2) {
          case SOUTH -> {
            return Optional.of(RailShape.SOUTH_WEST);
          }
          case NORTH -> {
            return Optional.of(RailShape.NORTH_WEST);
          }
          case EAST -> {
            return Optional.of(RailShape.EAST_WEST);
          }
        }
      }
      case EAST -> {
        switch (d2) {
          case SOUTH -> {
            return Optional.of(RailShape.SOUTH_EAST);
          }
          case WEST -> {
            return Optional.of(RailShape.EAST_WEST);
          }
          case NORTH -> {
            return Optional.of(RailShape.NORTH_EAST);
          }
        }
      }
    }
    return Optional.empty();
  }

  private List<RailShape> getPossibleShapes() {
    ArrayList<RailShape> shapes = new ArrayList<>();
    var directions = this.rw.findAllSuitableNeighbourDirections();
    for (Direction direction : directions) {
      if (direction != this.fixedPart) {
        constructShape(fixedPart, direction).ifPresent(shapes::add);
      }

    }
    return shapes;
  }

  public void toggle() {
    // TODO: change block state of reciprocal block
    RailRemoteControlMod.LOGGER.info("switching at "+ this.rw.pos().toShortString()+ " fixed side " + this.fixedPart.toString());
    RailShape newShape = nextShape();
    rw.world().setBlockState(rw.pos(), rw.getState().with(Properties.RAIL_SHAPE, newShape));
  }
  public RailShape nextShape() {
    RailShape rs= this.rw.getShape();
    var possible = getPossibleShapes();
    for (int i = 0; i < possible.size(); i++) {
      if(possible.get(i) == rs){
        if(i+1 >= possible.size()) {
          return possible.get(0);
        } else{
          return possible.get(i+1);
        }
      }
    }
    return possible.get(0);
  }
}
