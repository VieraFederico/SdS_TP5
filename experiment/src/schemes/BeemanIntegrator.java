package schemes;

import models.Agent;
import java.util.List;

/**
 * Beeman for velocity-dependent forces, predictor-corrector [Teorica_4, p. 20], the only TSDMD
 * integrator of the engine (decision ★3 of DECISIONS.md):
 * r(t+Δt) = r(t) + v(t)Δt + (2/3)a(t)Δt² - (1/6)a(t-Δt)Δt²
 * v(t+Δt)_(predicted) = v(t) + (3/2)a(t)Δt - (1/2)a(t-Δt)Δt
 * a(t+Δt) from r(t+Δt) and v(t+Δt)_(predicted), then
 * v(t+Δt)_(corrected) = v(t) + (1/3)a(t+Δt)Δt + (5/6)a(t)Δt - (1/6)a(t-Δt)Δt
 */
final class BeemanIntegrator {
    interface Forces {
        void evaluate(List<Agent> agents);
    }

    private final double dt;
    private final double[] vx, vy;

    BeemanIntegrator(double dt, int n) {
        this.dt = dt;
        vx = new double[n];
        vy = new double[n];
    }

    void advance(List<Agent> agents, Forces forces) {
        for (Agent a : agents) {
            vx[a.id] = a.vx;
            vy[a.id] = a.vy;
            a.x += a.vx * dt + (2.0 / 3 * a.ax - 1.0 / 6 * a.previousAx) * dt * dt;
            a.y += a.vy * dt + (2.0 / 3 * a.ay - 1.0 / 6 * a.previousAy) * dt * dt;
            a.vx += (3.0 / 2 * a.ax - 1.0 / 2 * a.previousAx) * dt;
            a.vy += (3.0 / 2 * a.ay - 1.0 / 2 * a.previousAy) * dt;
        }
        forces.evaluate(agents);
        for (Agent a : agents) {
            double nextAx = a.fx / a.mass, nextAy = a.fy / a.mass;
            a.vx = vx[a.id] + (1.0 / 3 * nextAx + 5.0 / 6 * a.ax - 1.0 / 6 * a.previousAx) * dt;
            a.vy = vy[a.id] + (1.0 / 3 * nextAy + 5.0 / 6 * a.ay - 1.0 / 6 * a.previousAy) * dt;
            a.previousAx = a.ax;
            a.previousAy = a.ay;
            a.ax = nextAx;
            a.ay = nextAy;
        }
    }
}
