package hgs.tombstone.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import hgs.tombstone.components.ComponentMappers;
import hgs.tombstone.components.TweenComponent;
import hgs.tombstone.components.TweenSpec;

public class TweenSystem extends IteratingSystem {

	public TweenSystem() {
		super(Family.all(TweenComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		TweenComponent twc = ComponentMappers.tween.get(entity);
		Array<TweenSpec> finished = new Array<TweenSpec>();
		for (TweenSpec tws : twc.tweenSpecs) {
			tws.time += deltaTime / tws.period;
			if (tws.time > 1) {
				tws.tweenInterface.endTween(entity);
				switch (tws.cycle) {
					case ONCE:
						tws.time = Math.min(tws.time, 1);
						tws.finished = true;
						break;
					case LOOP:
						tws.time -= 1;
						tws.loops--;
						if (tws.loops <= 0) {
							tws.finished = true;
						}
						break;
					case INFLOOP:
						tws.time -= 1;
						break;
				}
				if (tws.reverse) {
					float temp = tws.start;
					tws.start = tws.end;
					tws.end = temp;
				}
				if (tws.finished) {
					finished.add(tws);
				}
			}
			tws.tweenInterface.applyTween(entity, tws.interp.apply(tws.start, tws.end, tws.time));
		}
		twc.tweenSpecs.removeAll(finished, false);
	}
}
