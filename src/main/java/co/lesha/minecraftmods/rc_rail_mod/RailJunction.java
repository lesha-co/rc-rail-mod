package co.lesha.minecraftmods.rc_rail_mod;

import net.minecraft.util.math.Direction;

import java.util.*;

public record RailJunction(RailWalker rw, Direction fixedPart) {
  @Override
  public String toString() {
    return "RailJunction{" +
      "rw=" + rw +
      ", fixedPart=" + fixedPart +
      '}';
  }

  private List<Direction> getPossibleDirections() {
    ArrayList<Direction> otherDirections = new ArrayList<>();
    var directions = this.rw.findAllSuitableNeighbourDirections();
    for (Direction direction : directions) {
      if (direction != this.fixedPart) {
        if (RailRemoteControlMod.constructShape(fixedPart, direction).isPresent()) {
          otherDirections.add(direction);
        }

      }

    }
    return otherDirections;
  }

  public void toggle() {
    // TODO: change block state of reciprocal block
    RailRemoteControlMod.LOGGER.info("switching " + this);
    var nextDirection = nextOhterDirection();
    var newShape = RailRemoteControlMod.constructShape(this.fixedPart, nextDirection);
    newShape.ifPresent(rw::replaceShape);

    rw.findNextRailInDirection(nextDirection)
      .ifPresent(next -> next.attract(nextDirection.getOpposite()));

  }

  private Direction nextOhterDirection() {
    var facings = this.rw.getFacings();
    facings.remove(this.fixedPart);
    var facing = facings.toArray()[0];
    var possible = getPossibleDirections();
    for (int i = 0; i < possible.size(); i++) {
      if (possible.get(i) == facing) {
        if (i + 1 >= possible.size()) {
          return possible.get(0);
        } else {
          return possible.get(i + 1);
        }
      }
    }
    return possible.get(0);
  }
}
