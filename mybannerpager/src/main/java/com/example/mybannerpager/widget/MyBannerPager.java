package com.example.mybannerpager.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.mybannerpager.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MyBannerPager extends RelativeLayout implements View.OnClickListener {
    private Context mContext;//全局上下文对象
    private Button mButton;//全局按钮
    private ViewPager vp_banner;//全局ViewPager
    private ArrayList<ImageView> mViewList = new ArrayList<>();//图像视图列表存储多个ImageView
    private Handler mHandler = new Handler(Looper.myLooper());//声明一个处理器对象，用来实现自动翻页
    private Integer mInterval = 3000;//默认轮播时间间隔,单位毫秒
    private int mScrollDuration = 1000;//默认滚动时间间隔,单位毫秒，值越小速度越快
    private boolean blockSwipe = false;   // 是否拦截本次滑动

    //todo:手指按下暂停轮播
    public MyBannerPager(Context context) {
        this(context, null);
    }

    public MyBannerPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    //todo:1.轮播切换回第一张图片时不回滚 2.按钮不遮挡图片
    //初始化视图
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        //加载布局文件
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_banner_pager, null);
        //获取控件
        vp_banner = view.findViewById(R.id.vp_banner);
        mButton = view.findViewById(R.id.btn_try_free);
        addView(view);

        // 设置自定义Scroller以调整翻页速度
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mContext, mScrollDuration);
            scrollerField.set(vp_banner, scroller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        //添加翻页监听
//        vp_banner.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                blockSwipe = (position == 0 || position == mViewList.size() - 2);
//            }
//        });
        // 按下暂停、松开恢复
        vp_banner.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //边界暂停 ->切换停止 ->无法继续滑动
                        //todo:边界暂停->切换在翻页完成后立即触发，
                        stop();
                        break;
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        start();  // 手指离开，继续
                        int current = vp_banner.getCurrentItem();
                        //到了边界，松开后立即切换
                        if (current >= mViewList.size() - 1 || current == 0) {
                            mHandler.post(mScroll);
                        }
                        break;
                }
                return false;// 点击事件是否继续传递给 ViewPager true：不传递被拦截 false：传递
            }
        });
    }


    //设置图像列表
    public void setImage(List<Integer> imageList) {
        //清空列表
        mViewList.clear();
        final int N = imageList.size();
        for (int i = 0; i < N + 2; i++) {
            int index = (i == 0) ? N - 1//头部补尾
                    : (i == N + 1) ? 0//尾部补头
                    : i - 1;
            //创建ImageView
            ImageView iv = new ImageView(mContext);
            //设置属性
            iv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            iv.setImageResource(imageList.get(index));
            //将该ImageView存入集合中
            mViewList.add(iv);
        }
        //设置翻页视图的图像适配器
        vp_banner.setAdapter(new ImageAdapter());
        //默认显示第一张图片
        vp_banner.setCurrentItem(1);
    }

    public void setOnclickListener(ButtonClickListener listener) {
        mListener = listener;
        mButton.setOnClickListener(this);
    }

    //声明一个按钮点击监听器对象
    private ButtonClickListener mListener;

    @Override
    public void onClick(View v) {
        mListener.onButtonClick();
    }

    public interface ButtonClickListener {
        void onButtonClick();
    }


    //定义一个图像翻页适配器
    private class ImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViewList.size();
        }

        //判断当前视图是否来自指定对象
        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        //从容器中销毁指定位置的页面
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }

        //实例化指定位置的页面,并将其添加到容器中
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }
    }

    private Runnable mScroll = new Runnable() {
        @Override
        public void run() {
            int current = vp_banner.getCurrentItem();
            if (current >= mViewList.size() - 1) {
                //当前显示的图片到了末尾，跳转到第一张图片
                vp_banner.setCurrentItem(1, false);
                //立即换页
                stop();
                mHandler.post(this);
            } else if (current == 0) {
                //当前显示到了头部
                vp_banner.setCurrentItem(mViewList.size() - 2, false);
                //立即换页
                stop();
                mHandler.post(this);
            } else {
                int next = current + 1;
                vp_banner.setCurrentItem(next, true);
                //延迟指定间隔后重复执行该方法
                stop();
                mHandler.postDelayed(this, mInterval);
            }
        }
    };

    public void start() {
        //启动轮播
        // 保证单例：先取消旧任务，再启动新任务
        mHandler.removeCallbacks(mScroll);
        mHandler.postDelayed(mScroll, mInterval);
    }

    public void stop() {
        //取消该轮播
        mHandler.removeCallbacks(mScroll);
    }

    //设置时间间隔
    public void setInterval(Integer interval) {
        this.mInterval = interval;
    }

    // 设置翻页动画时长(毫秒)
    public void setScrollDuration(int duration) {
        this.mScrollDuration = duration;
        // 动态更新滚动器速度
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mContext, duration);
            scrollerField.set(vp_banner, scroller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //自定义滚动器，控制ViewPager翻页速度
    private class FixedSpeedScroller extends Scroller {

        public FixedSpeedScroller(Context context, int duration) {
            super(context);
            mScrollDuration = duration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // 强制使用自定义的滚动时长
            super.startScroll(startX, startY, dx, dy, mScrollDuration);
        }
    }

}
