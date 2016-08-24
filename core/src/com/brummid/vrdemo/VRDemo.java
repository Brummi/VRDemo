package com.brummid.vrdemo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.brummid.vrcamera.RendererForVR;
import com.brummid.vrcamera.VRCamera;
import com.brummid.vrcamera.VRCameraInputAdapter;

public class VRDemo extends ApplicationAdapter implements RendererForVR{

	/*
	The VRCamera renders on this batch
	 */
	Batch batch;

	/*
	The VRCamera and the input handler
	 */
	VRCamera vrCamera;
	VRCameraInputAdapter vrCameraInputAdapter;

	/*
	The 3D utilities we need to render the example scene
	 */
	ModelInstance exampleScene;
	ModelBatch modelBatch;
	ModelBatch shadowBatch;
	DirectionalShadowLight shadowLight;
	Environment environment;


	public void create () {
		/*
		Standard procedure
		 */

		batch = new SpriteBatch();

		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -1f, -1f));

		environment.add((shadowLight = new DirectionalShadowLight(512, 512, 60f, 60f, .1f, 50f))
				.set(1f, 1f, 1f, -1f, -1f, -1f));
		environment.shadowMap = shadowLight;

		shadowBatch = new ModelBatch(new DepthShaderProvider());

		loadModel();

		/*
		Initialization of the VRCamera. Constructor (javadoc): FOV in deg, near, far, eyedistance, viewportWidth, viewportHeight, RendererForVR

		The VRCameraInputAdapter takes care of head tracking. It needs to be updated each frame.
		 */

		vrCamera = new VRCamera(67, 0.01f, 128f, 0.1f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), this);
		vrCamera.translate(0, 1.8f, 0);
		vrCamera.update();
		vrCameraInputAdapter = new VRCameraInputAdapter(vrCamera);
		vrCameraInputAdapter.setLogging(false);
	}

	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		/*
		The VRCamera does not set a projection matrix, but renders by itself. It can not set a projection matrix because it has to render both "eyes" besides each other.
		 */

		if(Gdx.graphics.getDeltaTime() > 0)vrCameraInputAdapter.update(Gdx.graphics.getDeltaTime());
		vrCamera.update();

		/*
		Note that the shadow isn´t being rendered on the screen but on the environment shadow map. That is why we don´t calculate the shadows in the renderForVR method.
		 */
		shadowLight.begin(Vector3.Zero, vrCamera.getDirection());
		shadowBatch.begin(shadowLight.getCamera());
		shadowBatch.render(exampleScene);
		shadowBatch.end();
		shadowLight.end();

		vrCamera.render(batch);
	}

	/*
	This method from the "RendererForVR" interface is used to render the 3D scene. The VRCamera calls this method twice, each time with a different camera.
	Place your render calls there.
	 */
	public void renderForVR(PerspectiveCamera perspectiveCamera) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(perspectiveCamera);

		modelBatch.render(exampleScene, environment);

		modelBatch.end();
	}

	/*
	The following methods are not essential for the use of VRCamera
	 */
	public void dispose () {
		batch.dispose();
	}

	public void loadModel() {
		AssetManager assetManager = new AssetManager();
		assetManager.load("ExampleSceneJoinedTris.g3db", Model.class);
		assetManager.finishLoading();
		exampleScene = new ModelInstance((Model)assetManager.get("ExampleSceneJoinedTris.g3db"));
	}
}
