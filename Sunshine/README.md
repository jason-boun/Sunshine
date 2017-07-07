#readme
sunshine 
1. ContentProvider + SQLite + LoaderManager 加载数据
2. PreferenceFragment 个性化设置
3. IntentServer实现后台更新天气
4. FirebaseJobDispatcher + JobServer 实现周期性后台更新天气数据
5. Notification 实现通知
6. UI + 资源设置。


一、ContentProvider + SQLite + LoaderManager 加载数据
	1、加载器LoaderManager
		(1)、实现LoadCallback()		重写下面方法
				onCreateLoader()
					实例化并返回一个新创建给指定ID的Loader对象；第一启动时调用
					{
						 AsyncTaskLoader 类似AsyncTask

						CursorLoader 是AsyncTaskLoader的子类，内部持有ForceLoadContentObserver变量，观察者来实现对数据源的数据更新(自动更新数据)
						{
								在我们使用CurSorLoader时大家都会考虑一种情况的处理—–当数据库发生变化时如何自动刷新当前UI，数据库在数据改变时通过ContentPorvider和ContentResolver发出通知，接着ContentProvider通知Cursor的观察者数据发生了变化，然后Cursor通知CursorLoader的观察者数据发生了变化，CursorLoader又通过ContentProvider加载新数据，完成后调用CursorAdapter的changeCursor()用新数据替换旧数据显示。
								自动更新实现步骤
									对获取的Cursor数据设置需要监听的URI（即，在ContentProvider的query()方法或者Loader的loadingBackground()方法中调用Cursor的setNotificationUri()方法）；
									在ContentProvider的insert()、update()、delete()等方法中调用ContentResolver的notifyChange()方法;
						}
					}

				onLoadFinished()
				load完成之后回调此方法；每次都调用

				onLoaderReset()
				当创建好的Loader被reset时调用此方法，会清空已绑定数据，此时CreatLoader会重新执行

		(2)、启动loader
			Activity初始化在oncreate()初始化，一个Activity或Fragment中LoaderManager管理一个或多个Loader实例，每个Activity或Fragment只有一个LoaderManager，我们可以在Activity的onCreate()或Fragment的onActivityCreated()里初始化一个Loader。例如：
			getLoaderManager().initLoader(id, bundle, new MyLoaderCallback()); id相同不会重新建立loader

	2、SQLite
		(1)、继承SQLiteOpenHelper，获取自定义的helper类。(主要任务是创建表格和数据库更新)
		(2)、使用:获取自定义helper对象，然后调用getwriteable()或者getReadable()获取SQLiteDatabase对象。


	3、ContentProvider(http://www.jianshu.com/p/f5ec75a9cfea)
		简介：ContentProvider一般为存储和获取数据提供统一的接口，可以在不同的应用程序之间共享数据。手机中存在多个Provider,为了方便用户管理,Android提供了ContentResolver来同意管理(使用URI来进行区分不同Provider) URI = content://{Authority}/{TablePath}/{id}
		URI匹配问题
		使用的是URI
		{
			private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			static {
				//该ContentProvider只识别这两条uri其他不识别
	      	  uriMatcher.addURI(WEATHER_AUTHOR, WeatherContract.PATH_WEATHER, TABLE_CODE);

	          uriMatcher.addURI(WEATHER_AUTHOR, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);
	   		}
		}
	   

		(1)、继承ContentProvider,重写onCreate\query\update\insert\delete\getType
		(2)、调用Context.getContentResolver().query()等方法获取Cusor对象，然后进行操作。

二、PreferenceFragmentCompat 个性化设置(自动保存)
	1、创建res/xml {preferences}.xml
	根节点中一定是<PreferenceScreen>   item主要是CheckBoxPreference, EditTextPreference, ListPreference三种
	2、addPreferencesFromResource(R.xml.preferences);加载创建的xml文件
	3、通过实现SharedPreferences.OnSharedPreferenceChangeListener， 来监听数据改变。
	4、preferenceScreen.getSharedPreferences()可以获取到sharepreferences对象，然后可以进行对数据的操作。

三、IntentServer实现后台更新天气
	1、区别于Server
		(1)、IntentServer创建一个工作线程来处理多线程任务
		(2)、并且不需要手动结束服务(stopSelf),任务全部完成后自动关闭服务。
	2、启动
		(1)、定义IntentService的子类：传入线程名称、复写onHandleIntent()方法
		(2)、在Manifest.xml中注册服务
		(3)、在Activity中开启Service服务

四、FirebaseJobDispatcher + JobServer 实现周期性后台更新天气数据
	1、周期性后台更新常用方法：Alarm机制
		建立一个一直运行的后台server，然后通过Alarm周期性的发送广播(通过PendingIntent)
		(1)、获取系统服务AlarmManager manager = getSystemServer(Context.ALARM_SERVICE)
		(2)、manager.set(AlarmManager.RTC_WAKEUP, {任务触发时间}, pendingIntent)
	2、

	

五、Notification 实现通知
	1、获取NotificationManager manager = (Notification)getSystemServer(NOTIFICATION_SERVICE);
	2、获取Notification notification = new Notification(R.drawable, requestCode, "this is ticker text", System.currentTimeMillis());
	3、创建PendingIntent(在一定时机下执行动作)	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flag) 
	3、设置点击行为 notification.setLatestEvenInfo(context, "this is content title", "this is content text", pendingIntent);
	4、最后调用manager.notify(id, notification)
