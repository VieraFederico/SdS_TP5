package models;

import java.util.Arrays;

/**
 * Cell index method [Teorica_1, diapos 19-25], from the TP4 CellGrid, for the non-periodic rectangle
 * that holds the room and the 1 m outside it. Cells are at least one cutoff wide, so every pair
 * closer than the cutoff shares a cell or sits in adjacent cells. Agents are linked per cell and
 * move one at a time, because event-driven agents change cell at their own instants.
 */
public final class CellGrid {
    private final int columns, rows;
    private final double cellWidth, cellHeight, bottom, width;
    private final int[] head, next, previous, cellOf;
    private final Agent[] agents;

    public CellGrid(Room room, double cutoff, int n) {
        bottom = -Room.OUTER_TARGET_DEPTH;
        width = room.side;
        double height = room.side - bottom;
        columns = Math.max(1, (int) Math.floor(width / cutoff));
        rows = Math.max(1, (int) Math.floor(height / cutoff));
        cellWidth = width / columns;
        cellHeight = height / rows;
        head = new int[columns * rows];
        Arrays.fill(head, -1);
        next = new int[n];
        previous = new int[n];
        cellOf = new int[n];
        Arrays.fill(cellOf, -1);
        agents = new Agent[n];
    }

    public void insert(Agent agent) {
        agents[agent.id] = agent;
        link(agent.id, cell(agent));
    }

    public void remove(Agent agent) {
        unlink(agent.id);
        cellOf[agent.id] = -1;
    }

    /** Re-files the agent after it moved; costs nothing while it stays in its cell. */
    public void update(Agent agent) {
        int cell = cell(agent);
        if (cell == cellOf[agent.id]) return;
        unlink(agent.id);
        link(agent.id, cell);
    }

    /** Index of the cell the agent is filed in, -1 if removed. */
    public int cellOf(Agent agent) {
        return cellOf[agent.id];
    }

    /** Fills `out` with the agents in the 3×3 cells around `agent`, itself excluded; returns how many. */
    public int neighbors(Agent agent, Agent[] out) {
        int cell = cellOf[agent.id], column = cell % columns, row = cell / columns, count = 0;
        for (int r = Math.max(0, row - 1); r <= Math.min(rows - 1, row + 1); r++) {
            for (int c = Math.max(0, column - 1); c <= Math.min(columns - 1, column + 1); c++) {
                for (int j = head[r * columns + c]; j >= 0; j = next[j]) {
                    if (j != agent.id) out[count++] = agents[j];
                }
            }
        }
        return count;
    }

    /** Time until the agent leaves its cell in free flight. */
    public double exitTime(Agent agent) {
        int cell = cellOf[agent.id], column = cell % columns, row = cell / columns;
        double x = wall(agent.x, agent.vx, column, columns, cellWidth, 0);
        double y = wall(agent.y, agent.vy, row, rows, cellHeight, bottom);
        return Math.min(x, y);
    }

    /** Border cells have no exit on their outer side: positions past the rectangle stay in them. */
    private static double wall(double position, double speed, int index, int count, double size, double origin) {
        if (speed > 0 && index < count - 1) return Math.max(0, (origin + (index + 1) * size - position) / speed);
        if (speed < 0 && index > 0) return Math.max(0, (origin + index * size - position) / speed);
        return Double.POSITIVE_INFINITY;
    }

    private void link(int id, int cell) {
        cellOf[id] = cell;
        previous[id] = -1;
        next[id] = head[cell];
        if (head[cell] >= 0) previous[head[cell]] = id;
        head[cell] = id;
    }

    private void unlink(int id) {
        int cell = cellOf[id];
        if (previous[id] >= 0) next[previous[id]] = next[id];
        else head[cell] = next[id];
        if (next[id] >= 0) previous[next[id]] = previous[id];
    }

    /** Positions slightly outside the rectangle fall into the border cells. */
    private int cell(Agent agent) {
        int column = Math.max(0, Math.min(columns - 1, (int) (agent.x / cellWidth)));
        int row = Math.max(0, Math.min(rows - 1, (int) ((agent.y - bottom) / cellHeight)));
        return row * columns + column;
    }
}
