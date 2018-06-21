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

    static { <font color=#0099ff>// 静态语句块，第一次类加载时就初始化，且只执行一次</font>
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
