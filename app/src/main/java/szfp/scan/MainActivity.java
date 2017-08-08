package szfp.scan;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.sunmi.scan.Config;
import com.sunmi.scan.Image;
import com.sunmi.scan.ImageScanner;
import com.sunmi.scan.Symbol;
import com.sunmi.scan.SymbolSet;


public class MainActivity extends Activity implements SurfaceHolder.Callback {
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private SurfaceView surface_view;
	private ImageScanner scanner;//声明扫描器
	private Handler autoFocusHandler;
	private AsyncDecode asyncDecode;
    SoundUtils soundUtils;
    private boolean vibrate;
    public int decode_count=0;
	
	private FinderView finder_view;
	private TextView textview;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_sunmi_scan_finder);
		//Toast.makeText(getBaseContext(), "" + a, Toast.LENGTH_SHORT).show();
		init();
	}

	private void init() {
		surface_view = (SurfaceView) findViewById(R.id.surface_view);
		finder_view = (FinderView) findViewById(R.id.finder_view);
		textview = (TextView) findViewById(R.id.textview);
		mHolder = surface_view.getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.addCallback(this);

		scanner = new ImageScanner();//创建扫描器
		scanner.setConfig(0, Config.X_DENSITY, 2);//行扫描间隔
		scanner.setConfig(0, Config.Y_DENSITY, 2);//列扫描间隔
		scanner.setConfig(0, Config.ENABLE_MULTILESYMS, 0);//是否开启同一幅图一次解多个条码,0表示只解一个，1为多个
		scanner.setConfig(0, Config.ENABLE_INVERSE, 0);//是否解反色的条码
		scanner.setConfig( Symbol.PDF417,Config.ENABLE, 0);//是否禁止PDF417码，默认开启

		autoFocusHandler = new Handler();
		asyncDecode = new AsyncDecode();
		decode_count=0;	
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mHolder.getSurface() == null) {
			return;
		}
		try{
			mCamera.stopPreview();
		} catch (Exception e) {
		}
		try {		
			//摄像头预览分辨率设置和图像放大参数设置，非必须，根据实际解码效果可取舍
			Camera.Parameters parameters = mCamera.getParameters();  
            parameters.setPreviewSize(800, 480);  //设置预览分辨率
       //     parameters.set("zoom", String.valueOf(27 / 10.0));//放大图像2.7倍
            mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(90);//竖屏显示
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(previewCallback);
			mCamera.startPreview();
			mCamera.autoFocus(autoFocusCallback);
		} 
		catch (Exception e) {
			Log.d("DBG", "Error starting camera preview: " + e.getMessage());
		}
	}

	/**
	 * 预览数据
	 */
	PreviewCallback previewCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (asyncDecode.isStoped()){	
				Camera.Parameters parameters = camera.getParameters();
				Size size = parameters.getPreviewSize();//获取预览分辨率
                
				//创建解码图像，并转换为原始灰度数据，注意图片是被旋转了90度的
				Image source = new Image(size.width, size.height, "Y800");
				Rect scanImageRect = finder_view.getScanImageRect(size.height, size.width);
				//图片旋转了90度，将扫描框的TOP作为left裁剪
				source.setCrop(scanImageRect.top, scanImageRect.left, scanImageRect.height(), scanImageRect.width());
				source.setData(data);//填充数据
				asyncDecode = new AsyncDecode();
				asyncDecode.execute(source);//调用异步执行解码
			}
		}
	};

	private class AsyncDecode extends AsyncTask<Image, Void, Void> {
		private boolean stoped = true;
		private String str = "";

		@Override
		protected Void doInBackground(Image... params) {
			stoped = false;
			StringBuilder sb = new StringBuilder();
			Image src_data = params[0];//获取灰度数据
			
			long startTimeMillis = System.currentTimeMillis();
			
			//解码，返回值为0代表失败，>0表示成功
			int nsyms = scanner.scanImage(src_data);
			
			long endTimeMillis = System.currentTimeMillis();		
			long cost_time = endTimeMillis-startTimeMillis;
							
			if (nsyms != 0) {
				playBeepSoundAndVibrate();//解码成功播放提示音
				
				decode_count++;
				sb.append("count: " + String.valueOf(decode_count) + ", consuming: " + String.valueOf(cost_time) + " ms \n" );
				
				SymbolSet syms = scanner.getResults();//获取解码结果
				for (Symbol sym : syms) {	
					sb.append("[ " + sym.getSymbolName() + " ]: " + sym.getResult() + "\n");
				}
			}
			str = sb.toString();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			stoped = true;
			if (null == str || str.equals("")) {
			} else {
				textview.setText(str);//显示解码结果
			}
		}

		public boolean isStoped() {
			return stoped;
		}
	}

	/**
	 * 自动对焦回调
	 */
	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	//自动对焦
	private Runnable doAutoFocus = new Runnable() 
	{
		public void run() {
			if (null == mCamera || null == autoFocusCallback) {
				return;
			}
			mCamera.autoFocus(autoFocusCallback);
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try	{
			mCamera = Camera.open();
		} catch (Exception e){
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null){
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}
	
 	private void initBeepSound(){
		if(soundUtils == null){
			soundUtils = new SoundUtils(this, SoundUtils.RING_SOUND);
			soundUtils.putSound(0, R.raw.beep);
		}
 	}
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		initBeepSound();
 		vibrate= false;
 	}
 	/**
 	 * When the beep has finished playing, rewind to queue up another one.
 	 */
 	/*private final OnCompletionListener beepListener = new OnCompletionListener() {
 		public void onCompletion(MediaPlayer mediaPlayer) {
 			mediaPlayer.seekTo(0);
 		}
 	};*/
 	
 	private static final long VIBRATE_DURATION = 200L;
 	private void playBeepSoundAndVibrate() {
		if(soundUtils != null){
			soundUtils.playSound(0, SoundUtils.SINGLE_PLAY);
		}		
		if (vibrate){
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
 	}
 	
}
