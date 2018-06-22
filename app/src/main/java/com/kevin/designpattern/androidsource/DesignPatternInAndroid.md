基于API 26
### SystemService     (Singleton)

```
    // 获取系统的Service
    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
```

Context的实现类为ContextImpl
```
    @Override
    public Object getSystemService(String name) {
        return SystemServiceRegistry.getSystemService(this, name);
    }

final class SystemServiceRegistry {

    // Service registry information.
    // This information is never changed once static initialization has completed.
    private static final HashMap<Class<?>, String> SYSTEM_SERVICE_NAMES = new HashMap<Class<?>, String>();
    private static final HashMap<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS = new HashMap<String, ServiceFetcher<?>>();

    ...

    static { // 静态语句块，第一次类加载时就初始化，且只执行一次
        registerService(Context.ACCESSIBILITY_SERVICE, AccessibilityManager.class,
                new CachedServiceFetcher<AccessibilityManager>() {
            @Override
            public AccessibilityManager createService(ContextImpl ctx) {
                return AccessibilityManager.getInstance(ctx);
            }});

        registerService(Context.CAPTIONING_SERVICE, CaptioningManager.class,
                new CachedServiceFetcher<CaptioningManager>() {
            @Override
            public CaptioningManager createService(ContextImpl ctx) {
                return new CaptioningManager(ctx);
            }});
        ...
    }

```

### LayoutInflater && Activity.setContentView()

context.getSystemService()获得的LayoutInflater的对象是PhoneLayoutInflater实例
```
public class PhoneLayoutInflater extends LayoutInflater {
    private static final String[] sClassPrefixList = { // 系统View类的前缀
        "android.widget.",
        "android.webkit.",
        "android.app."
    };

    /** Override onCreateView to instantiate names that correspond to the
        widgets known to the Widget factory. If we don't find a match,
        call through to our super class.
    */
    @Override protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        for (String prefix : sClassPrefixList) {
            try {
                View view = createView(name, prefix, attrs);
                if (view != null) {
                    return view;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return super.onCreateView(name, attrs);
    }
}
```

Activity.setContentView():
```
    public void setContentView(@LayoutRes int layoutResID) {
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }
```

PhoneWindow.setContentView():
```
    @Override
    public void setContentView(int layoutResID) {
        // Note: FEATURE_CONTENT_TRANSITIONS may be set in the process of installing the window
        // decor, when theme attributes and the like are crystalized. Do not check the feature
        // before this happens.
        if (mContentParent == null) {
            installDecor(); // 初始化DecorView,通过Window的Feature来确定layoutResource id    mContentParent = generateLayout(mDecor);
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }

        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            mLayoutInflater.inflate(layoutResID, mContentParent); // 将activity的layout加载到DecorView中
        }
        ...
    }
```

LayoutInflater.inflate():
```
    public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        ...
        final XmlResourceParser parser = res.getLayout(resource); // 加载资源
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }


    public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            final Context inflaterContext = mContext;
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            Context lastContext = (Context) mConstructorArgs[0];
            mConstructorArgs[0] = inflaterContext;
            View result = root;

            try {
                // Look for the root node.
                int type;
                while ((type = parser.next()) != XmlPullParser.START_TAG &&
                        type != XmlPullParser.END_DOCUMENT) {
                    // Empty
                }

                final String name = parser.getName();

                if (TAG_MERGE.equals(name)) { // merge标签
                    if (root == null || !attachToRoot) { // 布局中包含merge标签时，attachToRoot参数必须为true
                        throw new InflateException("<merge /> can be used only with a valid "
                                + "ViewGroup root and attachToRoot=true");
                    }
                    rInflate(parser, root, inflaterContext, attrs, false);
                } else {
                    // Temp is the root view that was found in the xml
                    final View temp = createViewFromTag(root, name, inflaterContext, attrs);

                    ViewGroup.LayoutParams params = null;

                    if (root != null) {
                        // Create layout params that match root, if supplied
                        params = root.generateLayoutParams(attrs);
                        if (!attachToRoot) {
                            // Set the layout params for temp if we are not
                            // attaching. (If we are, we use addView, below)
                            temp.setLayoutParams(params);
                        }
                    }

                    // Inflate all children under temp against its context.
                    rInflateChildren(parser, temp, attrs, true);

                    // We are supposed to attach all the views we found (int temp)
                    // to root. Do that now.
                    if (root != null && attachToRoot) {
                        root.addView(temp, params);
                    }

                    // Decide whether to return the root that was passed in or the
                    // top view found in xml.
                    if (root == null || !attachToRoot) {
                        result = temp;
                    }
                }
            } finally {
                // Don't retain static reference on context.
                mConstructorArgs[0] = lastContext;
                mConstructorArgs[1] = null;
                Trace.traceEnd(Trace.TRACE_TAG_VIEW);
            }

            return result;
        }
    }



    View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
            boolean ignoreThemeAttr) {
        try {
            View view;
            // 用户可以通过设置LayoutInflater的factory来自行解析View,默认为空
            if (mFactory2 != null) {
                view = mFactory2.onCreateView(parent, name, context, attrs);
            } else if (mFactory != null) {
                view = mFactory.onCreateView(name, context, attrs);
            } else {
                view = null;
            }
            if (view == null && mPrivateFactory != null) {
                view = mPrivateFactory.onCreateView(parent, name, context, attrs);
            }
            if (view == null) {
                try {
                    if (-1 == name.indexOf('.')) { // 解析android原生View
                        view = onCreateView(parent, name, attrs);
                    } else { // 解析自定义的View
                        view = createView(name, null, attrs);
                    }
                }
            }
            return view;
        }
        ...
    }


// Inflate all children under temp against its context.
// rInflateChildren(parser, temp, attrs, true);  -> rInflate()

    void rInflate(XmlPullParser parser, View parent, Context context,
            AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {

        final int depth = parser.getDepth(); // 深度优先来构建视图树
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            final String name = parser.getName();

            if (TAG_REQUEST_FOCUS.equals(name)) {
                pendingRequestFocus = true;
                consumeChildElements(parser);
            } else if (TAG_TAG.equals(name)) {
                parseViewTag(parser, parent, attrs);
            } else if (TAG_INCLUDE.equals(name)) {
                if (parser.getDepth() == 0) {
                    throw new InflateException("<include /> cannot be the root element");
                }
                parseInclude(parser, context, parent, attrs);
            } else if (TAG_MERGE.equals(name)) {
                throw new InflateException("<merge /> must be the root element");
            } else {
                final View view = createViewFromTag(parent, name, context, attrs);
                final ViewGroup viewGroup = (ViewGroup) parent;
                final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
                // 递归调用
                rInflateChildren(parser, view, attrs, true);
                viewGroup.addView(view, params);
            }
        }
        if (finishInflate) {
            parent.onFinishInflate();
        }
    }
```

### WindowManger

```
    // Dialog的构造函数
    Dialog(@NonNull Context context, @StyleRes int themeResId, boolean createContextThemeWrapper) {
        ...
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final Window w = new PhoneWindow(mContext);
        mWindow = w;
        // 设置dialog Window的WindowManger
        w.setWindowManager(mWindowManager, null, null);
    }


    // Window的setWindowManager()
    public void setWindowManager(WindowManager wm, IBinder appToken, String appName,
            boolean hardwareAccelerated) {
        ...
        // 此时将WindowManager与Window关联起来
        mWindowManager = ((WindowManagerImpl)wm).createLocalWindowManager(this);
    }
```

WindowManagerImpl：
```
public final class WindowManagerImpl implements WindowManager {
    private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
    ...
    // 在ConextImpl(SystemServiceRegistry)中注册时调用的是此方法，parentWindow为空
    public WindowManagerImpl(Context context) {
        this(context, null);
    }

    private WindowManagerImpl(Context context, Window parentWindow) {
        mContext = context;
        mParentWindow = parentWindow;
    }

    public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
        return new WindowManagerImpl(mContext, parentWindow);
    }

    public WindowManagerImpl createPresentationWindowManager(Context displayContext) {
        return new WindowManagerImpl(displayContext, mParentWindow);
    }

    // WindowManger只是个对外暴露的类，具体的工作还是在WindowManagerGlobal中完成
    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

    @Override
    public void updateViewLayout(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.updateViewLayout(view, params);
    }

    @Override
    public void removeView(View view) {
        mGlobal.removeView(view, false);
    }

    @Override
    public void removeViewImmediate(View view) {
        mGlobal.removeView(view, true);
    }
    ...
}
```

WindowManagerGlobal.addView() && 会在ViewRootImpl的构造函数中调用的getWindowSession():
```
    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
        ...
        ViewRootImpl root;
        View panelParentView = null;

        synchronized (mLock) {
            ...
            root = new ViewRootImpl(view.getContext(), display);

            view.setLayoutParams(wparams);

            mViews.add(view);
            mRoots.add(root);
            mParams.add(wparams);

            // do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
                ...
            }
        }
    }


    public static IWindowSession getWindowSession() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowSession == null) {
                try {
                    InputMethodManager imm = InputMethodManager.getInstance();
                    // 获取WindowManagerService
                    IWindowManager windowManager = getWindowManagerService();
                    // 与WindowManagerService建立一个Session
                    sWindowSession = windowManager.openSession(
                            new IWindowSessionCallback.Stub() {
                                @Override
                                public void onAnimatorScaleChanged(float scale) {
                                    ValueAnimator.setDurationScale(scale);
                                }
                            },
                            imm.getClient(), imm.getInputContext());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowSession;
        }
    }

    public static IWindowManager getWindowManagerService() {
        synchronized (WindowManagerGlobal.class) {
            if (sWindowManagerService == null) {
                sWindowManagerService = IWindowManager.Stub.asInterface(
                        ServiceManager.getService("window"));
                try {
                    if (sWindowManagerService != null) {
                        ValueAnimator.setDurationScale(
                                sWindowManagerService.getCurrentAnimatorScale());
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return sWindowManagerService;
        }
    }
```
ServiceManager.getService():
ServiceManager.getService()返回的也是IBinder对象，说明Android FrameWork与WMS也是通过Bindle机制通信
```
    public static IBinder getService(String name) {
        try {
            IBinder service = sCache.get(name);
            if (service != null) {
                return service;
            } else {
                return Binder.allowBlocking(getIServiceManager().getService(name));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "error in getService", e);
        }
        return null;
    }
```

ViewRootImpl: ViewRootImpl并不是view，是作为native层与Java层View系统通信的桥梁
```
/**
 * The top of a view hierarchy, implementing the needed protocol between View
 * and the WindowManager.  This is for the most part an internal implementation
 * detail of {@link WindowManagerGlobal}.
 *
 * {@hide}
 */
@SuppressWarnings({"EmptyCatchBlock", "PointlessBooleanExpression"})
public final class ViewRootImpl implements ViewParent,
        View.AttachInfo.Callbacks, ThreadedRenderer.DrawCallbacks {

    public ViewRootImpl(Context context, Display display) {
        // 获取WindowSession，与WindowManagerService建立连接
        mWindowSession = WindowMan,agerGlobal.getWindowSession();
        // 保存当前线程，更新UI的线程只能是创建ViewRootImpl时的线程
        // 因为ViewRootImpl是在UI线程中创建的，所以更新也只能在UI线程
        mThread = Thread.currentThread();
    }


    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        synchronized (this) {
            if (mView == null) {
                ...
                // Schedule the first layout -before- adding to the window
                // manager, to make sure we do the relayout before receiving
                // any other events from the system.
                requestLayout();

                try {
                    res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                            getHostVisibility(), mDisplay.getDisplayId(),
                            mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
                            mAttachInfo.mOutsets, mInputChannel);
                } catch (RemoteException e) {
                }
                ...
            }
        }
    }

    @Override
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
            checkThread(); //
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }

    // 最终会走到
    void doTraversal() {
        if (mTraversalScheduled) {
            performTraversals();
        }
    }

    private void performTraversals() {
        // 1、获取Surface对象，用于图形绘制
        // 2、丈量整个视图树的各个View的大小，performMeasure()函数，   会走到View的mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        // 3、布局整个视图树，performLayout()函数    会走到View的host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
        // 4、绘制整个视图树，performDraw()函数
    }

    // 4、绘制
    private void performDraw() {
        final boolean fullRedrawNeeded = mFullRedrawNeeded;

        Trace.traceBegin(Trace.TRACE_TAG_VIEW, "draw");
        try {
            // 具体的绘制函数
            draw(fullRedrawNeeded);
        } finally {
        }
    }

    private void draw(boolean fullRedrawNeeded) {
        // 获取绘制表面
        Surface surface = mSurface;

        // 绘图表面需要更新
        if (!dirty.isEmpty() || mIsAnimating || accessibilityFocusDirty) {
            // 使用GPU绘制，就是开启硬件加速
            if (mAttachInfo.mThreadedRenderer != null && mAttachInfo.mThreadedRenderer.isEnabled()) {
                mAttachInfo.mThreadedRenderer.draw(mView, mAttachInfo, this);
            } else {
                // 使用CPU绘制
                if (!drawSoftware(surface, mAttachInfo, xOffset, yOffset, scalingRequired, dirty)) {
                    return;
                }
            }
        }

        if (animating) {
            mFullRedrawNeeded = true;
            scheduleTraversals();
        }
    }
}
```
<br/>

<img src="./imgs/UML.png" align="center" /> <br/>










