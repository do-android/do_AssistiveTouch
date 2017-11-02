package doext.implement;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoImageLoadHelper;
import core.helper.DoJsonHelper;
import core.helper.DoTextHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_AssistiveTouch_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_AssistiveTouch_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_AssistiveTouch_Model extends DoSingletonModule implements do_AssistiveTouch_IMethod, OnTouchListener {

	private WindowManager wm = null;
	private WindowManager.LayoutParams wmParams = null;

	private Activity ctx;

	private ImageView floatView;
	private double screenHeight;
	private double screenWidth;

	// ImageView的alpha值
	private int mAlpha = 255;
	private boolean isHide;

	private double xZoom;
	private double yZoom;

	public do_AssistiveTouch_Model() throws Exception {
		super();
		this.ctx = DoServiceContainer.getPageViewFactory().getAppContext();
		Rect r = new Rect();
		this.ctx.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);

		this.screenWidth = r.width();
		this.screenHeight = r.height();

		// 获取WindowManager
		wm = (WindowManager) ctx.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		// 设置LayoutParams(全局变量）相关参数
		wmParams = new WindowManager.LayoutParams();

		wmParams.type = LayoutParams.TYPE_PHONE; // 设置window type
		wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
		// 设置Window flag
		wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;

		// 设置悬浮窗口长宽数据
		wmParams.width = -2;
		wmParams.height = -2;
		wmParams.gravity = Gravity.TOP | Gravity.LEFT;

	}

	/**
	 * 图片渐变显示处理
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1 && mAlpha < 255) {
				mAlpha += 50;
				if (mAlpha > 255)
					mAlpha = 255;
				floatView.setAlpha(mAlpha);
				if (!isHide && mAlpha < 255)
					mHandler.sendEmptyMessageDelayed(1, 100);
			} else if (msg.what == 0 && mAlpha > 100) {
				mAlpha -= 10;
				if (mAlpha < 100)
					mAlpha = 100;
				floatView.setAlpha(mAlpha);
				if (isHide && mAlpha > 0)
					mHandler.sendEmptyMessageDelayed(0, 100);
			}
		}
	};

	private void showFloatView() {
		isHide = false;
		mHandler.sendEmptyMessage(1);
	}

	private void hideFloatView() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1500);
					isHide = true;
					mHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("showView".equals(_methodName)) {
			this.showView(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("hideView".equals(_methodName)) {
			this.hideView(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}

		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		// ...do something
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 隐藏辅助按钮；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void hideView(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (this.floatView != null) {
			this.floatView.setVisibility(View.GONE);
		}
	}

	/**
	 * 显示辅助按钮；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void showView(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		double _x;
		double _y;

		xZoom = screenWidth / DoServiceContainer.getGlobal().getDesignScreenWidth();
		yZoom = screenHeight / DoServiceContainer.getGlobal().getDesignScreenHeight();

		double _zoom = Math.min(xZoom, yZoom);

		String _location = DoJsonHelper.getString(_dictParas, "location", "");
		if (TextUtils.isEmpty(_location)) {
			throw new Exception("AssistiveTouch showView location 参数不能为空！");
		}
		String[] _xy = _location.split(",");
		if (_xy.length == 2) {
			_x = DoTextHelper.strToDouble(_xy[0], screenWidth);
			_y = DoTextHelper.strToDouble(_xy[1], screenHeight);
		} else {
			throw new Exception("AssistiveTouch showView location 参数设置不正确！");
		}

		// 以屏幕左上角为原点，设置x、y初始值
		double[] _xory = checkPosition((int) (_x * xZoom), (int) (_y * yZoom), wmParams.height);
		wmParams.x = (int) _xory[0];
		wmParams.y = (int) _xory[1];
		isMove = DoJsonHelper.getBoolean(_dictParas, "isMove", true);
		if (floatView == null) {
			floatView = new ImageView(ctx.getApplicationContext());
			// 设置图片的触摸事件
			floatView.setOnTouchListener(this);
			// 显示myFloatView图像
			wm.addView(floatView, wmParams);
		}

		String _image = DoJsonHelper.getString(_dictParas, "image", "");
		if (!TextUtils.isEmpty(_image)) {
			_image = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), _image);
//			Bitmap _bitmap = BitmapFactory.decodeFile(_image);
			Bitmap _bitmap = DoImageLoadHelper.getInstance().loadLocal(_image, -1, -1);
			if (null != _bitmap) {
				floatView.setImageBitmap(_bitmap);
				floatView.setVisibility(View.VISIBLE);
				wmParams.width = (int) (_bitmap.getWidth() * _zoom);
				wmParams.height = (int) (_bitmap.getHeight() * _zoom);
				wm.updateViewLayout(floatView, wmParams);
			}
		}

		hideFloatView();
	}

	private int startX;
	private int startY;

	private int lastX;
	private int lastY;
	private boolean isMove = true;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // 手指触摸
			startX = (int) event.getRawX();
			startY = (int) event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE: // 手指移动
			showFloatView();
			if (!isMove) {
				return true;
			}
			int newX = (int) event.getRawX();
			int newY = (int) event.getRawY();

			int dx = newX - startX;
			int dy = newY - startY;

			wmParams.x = wmParams.x + dx;
			wmParams.y = wmParams.y + dy;
			wm.updateViewLayout(floatView, wmParams);

			startX = (int) event.getRawX();
			startY = (int) event.getRawY();

			break;
		case MotionEvent.ACTION_UP: // 手指离开的时候
			hideFloatView();
			int _lastx = wmParams.x;
			int _lasty = wmParams.y;

			double _xy[] = checkPosition(_lastx, _lasty, floatView.getHeight());
			wmParams.x = (int) _xy[0];
			wmParams.y = (int) _xy[1];
			wm.updateViewLayout(floatView, wmParams);

			if (_lastx == this.lastX && _lasty == this.lastY) { // 判断是点击事件还是移动事件
				// touch
				getEventCenter().fireEvent("touch", new DoInvokeResult(getUniqueKey()));
				return false;
			}
			this.lastX = wmParams.x;
			this.lastY = wmParams.y;
			break;
		}
		return true;
	}

	private double[] checkPosition(int _x, int _y, int _h) {

		double[] xy = new double[2];

		if (_y < 200) { // 靠上
			xy[0] = _x;
			xy[1] = 0;
			return xy;
		}

		if ((_y + _h) >= (screenHeight - 200)) { // 靠下
			xy[0] = _x;
			xy[1] = screenHeight - _h;
			return xy;
		}

		if (_x <= (screenWidth / 2)) { // 靠左
			xy[0] = 0;
			xy[1] = _y;
			return xy;
		}

		if (_x > (screenWidth / 2)) { // 靠右
			xy[0] = screenWidth;
			xy[1] = _y;
			return xy;
		}

		return xy;

	}
}