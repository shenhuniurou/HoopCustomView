## HoopCustomView 仿虎扑直播比赛界面的打赏按钮

文章博客地址：[http://www.jianshu.com/p/d8a4e34e220b](http://www.jianshu.com/p/d8a4e34e220b)

**效果图：**

![hoopview](http://upload-images.jianshu.io/upload_images/1159224-d2787b7f019e3765.gif?imageMogr2/auto-orient/strip)

**使用方法：**

在xml中的使用：

```xml
<LinearLayout
	android:layout_width="match_parent"
	android:layout_height="wrap_content"
	android:layout_alignParentBottom="true"
	android:layout_marginBottom="20dp"
	android:layout_alignParentRight="true"
	android:orientation="vertical">

	<com.xx.hoopcustomview.HoopView
		android:id="@+id/hoopview1"
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:layout_marginRight="10dp"
		app:text="支持火箭"
		app:count="1358"
		app:theme_color="#31A129"/>

	<com.xx.hoopcustomview.HoopView
		android:id="@+id/hoopview2"
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:layout_marginRight="10dp"
		app:text="热火无敌"
		app:count="251"
		app:theme_color="#F49C11"/>
</LinearLayout>
```

activity中使用：

```java
hoopview1 = (HoopView) findViewById(R.id.hoopview1);
hoopview1.setOnClickButtonListener(new HoopView.OnClickButtonListener() {
	@Override public void clickButton(View view, int num) {
		Toast.makeText(MainActivity.this, "hoopview1增加了" + num, Toast.LENGTH_SHORT).show();
	}
});
```

