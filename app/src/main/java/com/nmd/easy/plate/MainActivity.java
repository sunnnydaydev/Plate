package com.nmd.easy.plate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private LuckyPlate luckyPlate;
    private AppCompatImageView imgPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        luckyPlate = findViewById(R.id.lucky_disc);
        imgPlay = findViewById(R.id.img_start_rotation);
    }

    private void initData() {
        String[] strings = new String[]{"托儿索", "儿童劫", "鱼尾雯", "小学僧"}; // 转盘内容
        luckyPlate.setContents(strings); // 设置数据

        imgPlay.setOnClickListener(new View.OnClickListener() {// 播放监听
            @Override
            public void onClick(View v) {
                luckyPlate.startRotate(-1);// 随机转动
            }
        });


        luckyPlate.addOnAnimatorListener(randomPos -> { // 动画完成监听：randomPos对应上String数组的索引
            Toast.makeText(this, "" + strings[randomPos], Toast.LENGTH_SHORT).show();
        });
    }


}
