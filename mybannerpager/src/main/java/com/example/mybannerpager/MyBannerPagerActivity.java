package com.example.mybannerpager;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mybannerpager.util.Utils;
import com.example.mybannerpager.widget.MyBannerPager;

import java.util.ArrayList;
import java.util.List;

public class MyBannerPagerActivity extends AppCompatActivity implements MyBannerPager.ButtonClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_banner_pager);
        //从布局文件中获取我的自定义控件
        MyBannerPager myBanner = findViewById(R.id.my_banner_pager);
        //获取并设置布局参数
        LinearLayout.LayoutParams params =(LinearLayout.LayoutParams) myBanner.getLayoutParams();
        params.height = (int) (Utils.getScreenWidth(this) * 250f / 240f);
        //设置按钮监听
        myBanner.setOnclickListener(this);
        //设置图片列表
        myBanner.setImage(getImageList());
        myBanner.start();

    }
    private List<Integer> getImageList(){
        ArrayList<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.b1);
        imageList.add(R.drawable.b2);
        imageList.add(R.drawable.b3);
        imageList.add(R.drawable.b4);
        imageList.add(R.drawable.b5);
        return imageList;
    }

    @Override
    public void onButtonClick() {
        Toast.makeText(this,"你点击了按钮",Toast.LENGTH_SHORT).show();
    }
}