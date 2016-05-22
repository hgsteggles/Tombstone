package hgs.tombstone.world;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import hgs.tombstone.assets.Assets;
import hgs.tombstone.assets.GameArt;
import hgs.tombstone.components.*;
import hgs.tombstone.elements.Enums.*;
import hgs.tombstone.elements.GameMusic;
import hgs.tombstone.elements.GameParameters;
import hgs.tombstone.screens.BasicScreen;
import hgs.tombstone.systems.MovementSystem;
import hgs.tombstone.systems.RenderingSystem;

/**
 * Created by harry on 01/04/16.
 */
public class World {
	private int sectionWidth = 20;
	private int miniBossSection = 3;
	private int bossSection = 4;
	private float playerStartX = 1.5f;
	public BossType boss;
	private MiniBossType miniBoss;
	private PlayerType player;
	private float levelSpeed;
	private int endPosX;
	private int level;
	private int npages = 0;

	public CountComponent timeCount;

	private ObjectMap<MusicType, GameMusic> musicMap;

	public World(int level, int npages) {
		this.npages = npages;
		this.level = level;

		if (level == 4) {
			bossSection = 4;
			miniBossSection = 1;
		}
		else if (level == 5) {
			bossSection = 1;
		}

		ObjectMap<Integer, BossType> bossMap = new ObjectMap<Integer, BossType>();
		bossMap.put(1, BossType.MELVIN);
		bossMap.put(2, BossType.JULIAN);
		bossMap.put(3, BossType.RENE);
		bossMap.put(4, BossType.SVEN);
		bossMap.put(5, BossType.ENDLESS);

		ObjectMap<Integer, MiniBossType> miniBossMap = new ObjectMap<Integer, MiniBossType>();
		miniBossMap.put(1, MiniBossType.IVA);
		miniBossMap.put(2, MiniBossType.GREG);
		miniBossMap.put(3, MiniBossType.ROB);
		miniBossMap.put(4, MiniBossType.MARC);

		boss = bossMap.get(level);
		miniBoss = miniBossMap.get(level);

		levelSpeed = GameParameters.baseLevelSpeed;
		endPosX = (bossSection - 1) * sectionWidth;

		player = PlayerType.FERNANDO;

		String lstr = Integer.toString(Math.min(level, 3));
		musicMap = new ObjectMap<MusicType, GameMusic>();

		musicMap.put(MusicType.RUNNING, new GameMusic(Assets.music.get("running-" + lstr)));
		musicMap.put(MusicType.BOSS, new GameMusic(Assets.music.get("boss-" + lstr)));

		musicMap.get(MusicType.RUNNING).setVolumeScale(0.2f);
		musicMap.get(MusicType.BOSS).setVolumeScale(0.2f);
	}

	public int getLevel() {
		return level;
	}

	public void populateLevel(Engine engine, OrthographicCamera worldCamera) {
		engine.addEntity(WorldUI.createBottomBar());
		engine.addEntity(WorldUI.createTopBar());
		engine.addEntity(WorldUI.createFullBlackBG());

		//engine.addEntity(WorldUI.createInstructions());
		float BUTTON_W = 2.5f;
		float BUTTON_H = 1.8f;
		float SPACE = (BasicScreen.WORLD_WIDTH - 3f * BUTTON_W) / 4f;
		engine.addEntity(WorldUI.createControlButton(1f * SPACE + 0.5f * BUTTON_W, 1f, BUTTON_W, BUTTON_H, ControlType.JUMP));
		engine.addEntity(WorldUI.createControlButton(2f * SPACE + 1.5f * BUTTON_W, 1f, BUTTON_W, BUTTON_H, ControlType.SLIDE));
		engine.addEntity(WorldUI.createControlButton(3f * SPACE + 2.5f * BUTTON_W, 1f, BUTTON_W, BUTTON_H, ControlType.FIRE));
		engine.addEntity(WorldUI.createText(1f * SPACE + 0.5f * BUTTON_W, 1f, "JUMP"));
		engine.addEntity(WorldUI.createText(2f * SPACE + 1.5f * BUTTON_W, 1f, "SLIDE"));
		engine.addEntity(WorldUI.createText(3f * SPACE + 2.5f * BUTTON_W, 1f, "FIRE"));

		if (level <= 3) {
			engine.addEntity(WorldUI.createPageCounter());
			engine.addEntity(WorldUI.createPageCounterIcon(0.5f, 4.5f, false));
			engine.addEntity(WorldUI.createChapter(level));
		}
		else if (level == 5) {
			Entity timer = WorldUI.createTimer();
			engine.addEntity(timer);
			timeCount = ComponentMappers.count.get(timer);
		}

		if (level < 5) {
			engine.addEntity(createRunningMusic());
		}

		engine.addEntity(createCamera(worldCamera));

		for (int i = 0; i < bossSection; ++i) {
			if (level <= 3)
				engine.addEntity(EntityFactory.createBackground((i + 0.5f) * sectionWidth));
			else {
				for (int j = 0; j < 10; ++j)
					engine.addEntity(EntityFactory.createTronBackground(i * sectionWidth + (j + 0.5f) * 2, levelSpeed));
			}
		}
		engine.addEntity(EntityFactory.createBattleBackground(endPosX + 0.5f * BasicScreen.WORLD_WIDTH));
		engine.addEntity(EntityFactory.createBossEventBox(endPosX));
		TextureRegion playerRegion = GameArt.playerRunning.first();
		engine.addEntity(EntityFactory.createMiniBossEventBox((miniBossSection - 1) * sectionWidth + playerStartX + playerRegion.getRegionWidth() * RenderingSystem.PIXELS_TO_WORLD));
		engine.addEntity(createPlayer());

		if (level == 3) {
			engine.addEntity(EntityFactory.createTombstone((bossSection - 1) * sectionWidth + BasicScreen.WORLD_WIDTH - 0.5f));
		}

		if (level == 1)
			populateLevelOne(engine);
		else if (level == 2)
			populateLevelTwo(engine);
		else if (level == 3) {
			populateLevelThree(engine);
		}
	}

	public int getNumberPages() {
		return npages;
	}

	public void addPage(int n) {
		npages += n;
	}

	public void removePage(int n) {
		npages -= n;
	}

	public void populateLevelOne(Engine engine) {
		float ipos = 6;
		engine.addEntity(EntityFactory.createPin(ipos));
		ipos += 0;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 3;
		engine.addEntity(EntityFactory.createBall(ipos));
		ipos += 2;
		engine.addEntity(EntityFactory.createPage(ipos, true));
		ipos += 2;
		engine.addEntity(EntityFactory.createStackedTable(ipos));
		ipos += 1;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 3;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 2;
		engine.addEntity(EntityFactory.createPin(ipos));
		ipos += 4;
		engine.addEntity(EntityFactory.createStackedTable(ipos));
		engine.addEntity(EntityFactory.createPage(ipos, true));
		ipos += 3;
		engine.addEntity(EntityFactory.createBall(ipos));
		ipos += 1;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 3;
		engine.addEntity(EntityFactory.createHangingStar(ipos));
		ipos += 1;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 2;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 1;
		engine.addEntity(EntityFactory.createHangingEarth(ipos));
		ipos += 4;
		engine.addEntity(EntityFactory.createPage(ipos, true));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createTonye(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, true));
	}

	public void populateLevelTwo(Engine engine) {
		float ipos = 5.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createStackedTable(ipos));
		ipos += 3;
		engine.addEntity(EntityFactory.createTable(ipos));
		engine.addEntity(EntityFactory.createPin(ipos));
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 3;
		engine.addEntity(EntityFactory.createBall(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 2.5f;
		engine.addEntity(EntityFactory.createHangingStar(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 2.0f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createHangingStar(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 2.0f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createHangingStar(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 2.5f;
		engine.addEntity(EntityFactory.createPin(ipos));
		ipos += 2.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createHangingEarth(ipos));
		engine.addEntity(EntityFactory.createStackedTable(ipos));
		ipos += 3;
		engine.addEntity(EntityFactory.createTable(ipos));
		engine.addEntity(EntityFactory.createBall(ipos));
		ipos += 4;
		engine.addEntity(EntityFactory.createPage(ipos, true));
		ipos += 2;
		engine.addEntity(EntityFactory.createCat(ipos, levelSpeed));
		engine.addEntity(EntityFactory.createKathryn(ipos));
	}

	public void populateLevelThree(Engine engine) {
		float ipos = 6;

		engine.addEntity(EntityFactory.createCat(ipos, levelSpeed));
		ipos += 3;
		engine.addEntity(EntityFactory.createBall(ipos));
		engine.addEntity(EntityFactory.createTable(ipos));
		ipos += 3;
		engine.addEntity(EntityFactory.createHangingEarth(ipos));
		engine.addEntity(EntityFactory.createTable(ipos));
		ipos += 3;
		engine.addEntity(EntityFactory.createPin(ipos));
		engine.addEntity(EntityFactory.createTable(ipos));
		ipos += 2;
		engine.addEntity(EntityFactory.createHangingEarth(ipos));
		ipos += 2;
		engine.addEntity(EntityFactory.createTable(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 1.0f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createHangingStar(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 1.0f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPin(ipos));
		ipos += 2;
		//engine.addEntity(EntityFactory.createHangingEarth(ipos));
		ipos += 2;
		engine.addEntity(EntityFactory.createStackedTable(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 5.5f;
		engine.addEntity(EntityFactory.createTableSimon(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, false));
		ipos += 4.5f;
		engine.addEntity(EntityFactory.createAbi(ipos));
		ipos += 0.5f;
		engine.addEntity(EntityFactory.createPage(ipos, true));
	}

	public Entity createLightning() {
		float posX = (bossSection - 1) * sectionWidth + BasicScreen.WORLD_WIDTH - 2 + playerStartX;
		return EntityFactory.createLightning(posX);
	}

	public Entity createMiniBoss() {
		Entity entity = new Entity();

		StateComponent stateComp = new StateComponent();
		stateComp.set(MiniBossState.PRIME.value());
		entity.add(stateComp);

		AnimationComponent animComp = new AnimationComponent();
		animComp.animations.put(MiniBossState.PRIME.value(), new Animation(0.1f, GameArt.miniBossPrime.get(miniBoss), Animation.PlayMode.NORMAL));
		animComp.animations.put(MiniBossState.THROW.value(), new Animation(0.1f, GameArt.miniBossThrow.get(miniBoss), Animation.PlayMode.NORMAL));
		entity.add(animComp);

		TextureComponent texComp = new TextureComponent();
		TextureRegion region = animComp.animations.get(stateComp.get()).getKeyFrame(0);
		texComp.region = region;
		texComp.size.x = (region.getRegionWidth()/(float)(region.getRegionHeight()));
		texComp.size.y = 1.0f;
		entity.add(texComp);

		TransformComponent transComp = new TransformComponent();
		float x = (miniBossSection - 1) * sectionWidth + BasicScreen.WORLD_WIDTH + playerStartX;
		float y = BasicScreen.WORLD_HEIGHT / 2.0f - 0.5f;
		float z = 1.0f;
		transComp.body.initPosition(x, y, z);
		entity.add(transComp);

		MovementComponent moveComp = new MovementComponent();
		moveComp.linearVelocity.x = 0.2f;
		float dx = 2f;
		float du = moveComp.linearVelocity.x - levelSpeed;
		moveComp.linearAcceleration.x = 0.5f * du * du / dx;
		float t = Math.abs(du / moveComp.linearAcceleration.x);
		entity.add(moveComp);

		CollisionComponent collComp = new CollisionComponent();
		collComp.type = CollisionType.ENEMY;
		collComp.rect.setSize(texComp.size.x, texComp.size.y);
		collComp.rect.setCenter(x, y);
		entity.add(collComp);

		TickerComponent tickComp = new TickerComponent();
		tickComp.finishActive = true;
		tickComp.duration = t;
		tickComp.finish = new EventInterface() {
			@Override
			public void dispatchEvent(Entity e) {
				e.add(ComponentFactory.createMiniBossGun());
			}
		};
		entity.add(tickComp);

		MiniBossComponent miniComp = new MiniBossComponent();
		miniComp.maxSpeed = levelSpeed;
		miniComp.who = miniBoss;
		entity.add(miniComp);

		return entity;
	}

	public Entity createBoss() {
		Entity entity = new Entity();

		StateComponent stateComp = new StateComponent();
		if (boss == BossType.RENE || boss == BossType.ENDLESS)
			stateComp.set(BossState.PRIME.value());
		else
			stateComp.set(BossState.ENTER.value());
		entity.add(stateComp);

		AnimationComponent animComp = new AnimationComponent();
		if (boss != BossType.RENE && boss != BossType.ENDLESS)
			animComp.animations.put(BossState.ENTER.value(), new Animation(0.15f, GameArt.bossEnter.get(boss), Animation.PlayMode.LOOP));
		animComp.animations.put(BossState.PRIME.value(), new Animation(0.1f, GameArt.bossPrime.get(boss), Animation.PlayMode.NORMAL));
		animComp.animations.put(BossState.THROW.value(), new Animation(0.1f, GameArt.bossThrow.get(boss), Animation.PlayMode.NORMAL));
		entity.add(animComp);

		TextureComponent texComp = new TextureComponent();
		TextureRegion region = animComp.animations.get(stateComp.get()).getKeyFrame(0);
		texComp.region = region;
		texComp.size.x = (region.getRegionWidth()/(float)(region.getRegionHeight()));
		texComp.size.y = 1.0f;
		if (boss == BossType.RENE || boss == BossType.ENDLESS)
			texComp.color.a = 0.0f;
		entity.add(texComp);

		TransformComponent transComp = new TransformComponent();
		float x = (bossSection - 1) * sectionWidth + BasicScreen.WORLD_WIDTH - 2 + playerStartX;
		float y = BasicScreen.WORLD_HEIGHT / 2.0f - 0.5f;
		float z = 1.0f;
		transComp.body.initPosition(x, y, z);
		entity.add(transComp);

		CollisionComponent collComp = new CollisionComponent();
		collComp.type = CollisionType.OBJECT;
		collComp.rect.setSize(texComp.size.x, texComp.size.y);
		collComp.rect.setCenter(x, y);
		entity.add(collComp);

		BossComponent bossComp = new BossComponent();
		bossComp.who = boss;
		bossComp.health = 1;
		entity.add(bossComp);

		if (boss == BossType.RENE || boss == BossType.ENDLESS) {
			TweenComponent tweenComponent = new TweenComponent();
			TweenSpec tweenSpec = new TweenSpec();
			tweenSpec.period = 1.0f;
			tweenSpec.tweenInterface = new TweenInterface() {
				@Override
				public void applyTween(Entity e, float a) {
					TextureComponent txc = ComponentMappers.texture.get(e);
					txc.color.a = a;
				}

				@Override
				public void endTween(Entity e) {
					TextureComponent txc = ComponentMappers.texture.get(e);
					txc.color.a = 1.0f;
				}
			};
			tweenComponent.tweenSpecs.add(tweenSpec);
			entity.add(tweenComponent);
		}

		if (boss ==  BossType.ENDLESS) {
			entity.add(new EndlessComponent());
		}

		return entity;
	}

	public Entity createPlayer() {
		Entity entity = new Entity();

		StateComponent stateComp = new StateComponent();
		stateComp.set(PlayerState.RUN.value());
		entity.add(stateComp);

		AnimationComponent animComp = new AnimationComponent();
		animComp.animations.put(PlayerState.IDLE.value(), new Animation(0.1f, GameArt.playerIdle, Animation.PlayMode.NORMAL));
		animComp.animations.put(PlayerState.JUMP.value(), new Animation(0.1f, GameArt.playerJumping, Animation.PlayMode.NORMAL));
		animComp.animations.put(PlayerState.RUN.value(), new Animation(0.1f, GameArt.playerRunning, Animation.PlayMode.LOOP));
		animComp.animations.put(PlayerState.SLIDE.value(), new Animation(0.1f, GameArt.playerSliding, Animation.PlayMode.NORMAL));
		animComp.animations.put(PlayerState.DEAD.value(), new Animation(0.1f, GameArt.playerSliding, Animation.PlayMode.NORMAL));
		entity.add(animComp);

		TextureComponent texComp = new TextureComponent();
		TextureRegion region = animComp.animations.get(PlayerState.RUN.value()).getKeyFrame(0);
		texComp.region = region;
		texComp.size.x = (region.getRegionWidth()/(float)(region.getRegionHeight()));
		texComp.size.y = 1.0f;
		entity.add(texComp);

		TransformComponent transComp = new TransformComponent();
		float x = playerStartX;
		float y = BasicScreen.WORLD_HEIGHT / 2.0f - 0.5f;
		float z = 1.0f;
		transComp.body.initPosition(x, y, z);
		entity.add(transComp);

		MovementComponent moveComp = new MovementComponent();
		moveComp.linearVelocity.x = levelSpeed;
		entity.add(moveComp);

		CollisionComponent collComp = new CollisionComponent();
		collComp.type = CollisionType.PLAYER;
		collComp.rect.setSize(0.9f * texComp.size.x, 0.9f * texComp.size.y);
		collComp.rect.setCenter(x, y);
		entity.add(collComp);

		entity.add(new JumpComponent());
		entity.add(new SlideComponent());

		PlayerComponent playerComp = new PlayerComponent();
		playerComp.character = player;
		playerComp.endX = endPosX + playerStartX;
		entity.add(playerComp);

		StopComponent stopComp = new StopComponent();
		stopComp.endX = endPosX + playerStartX;
		stopComp.stopInterface = new EventInterface() {
			@Override
			public void dispatchEvent(Entity e) {
				MovementSystem.idle(e);
				e.remove(StopComponent.class);
			}
		};
		entity.add(stopComp);

		if (level != 5)
			entity.add(ComponentFactory.createPlayerGun());

		BarrierComponent barrierComp = new BarrierComponent();
		barrierComp.rect.setSize(3.0f, 10.0f);
		barrierComp.rect.setCenter(x, y);
		entity.add(barrierComp);

		entity.add(new ControlComponent());
		entity.add(new KillObjectComponent());

		return entity;
	}

	public Entity createCamera(OrthographicCamera camera) {
		Entity entity = new Entity();

		TransformComponent transComp = new TransformComponent();
		transComp.body.initPosition(camera.position.x, camera.position.y, camera.position.z);
		entity.add(transComp);

		MovementComponent moveComp = new MovementComponent();
		//moveComp.rotationalVelocity = 40.0f;
		moveComp.linearVelocity.x = levelSpeed;
		entity.add(moveComp);

		CameraComponent camComp = new CameraComponent();
		camComp.camera = camera;
		camComp.endX = endPosX + 0.5f * BasicScreen.WORLD_WIDTH;
		entity.add(camComp);

		StopComponent stopComp = new StopComponent();
		stopComp.endX = endPosX + 0.5f * BasicScreen.WORLD_WIDTH;
		stopComp.stopInterface = new EventInterface() {
			@Override
			public void dispatchEvent(Entity e) {
				TransformComponent tc = ComponentMappers.transform.get(e);
				ShakeComponent shakeComp = new ShakeComponent();
				shakeComp.maxRadius = 0.05f;
				shakeComp.maxTime = 1f;
				shakeComp.posX = tc.body.getPosition().x;
				shakeComp.posY = tc.body.getPosition().y;
				e.add(shakeComp);

				e.remove(StopComponent.class);
			}
		};
		entity.add(stopComp);

		return entity;
	}

	public Entity createRunningMusic() {
		Entity entity = new Entity();

		MusicComponent mc = new MusicComponent();
		mc.music = musicMap.get(MusicType.RUNNING);
		mc.music.setVolume(0.0f);
		mc.music.play(true);
		mc.control = MusicControlType.FADEIN;
		mc.type = MusicType.RUNNING;
		entity.add(mc);

		return entity;
	}

	public Entity createBossMusic() {
		Entity entity = new Entity();

		MusicComponent mc = new MusicComponent();
		mc.music = musicMap.get(MusicType.BOSS);
		mc.music.setVolume(0.0f);
		mc.music.play(true);
		mc.control = MusicControlType.FADEIN;
		mc.type = MusicType.BOSS;
		entity.add(mc);

		return entity;
	}
}