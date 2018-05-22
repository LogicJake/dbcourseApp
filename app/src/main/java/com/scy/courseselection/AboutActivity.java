package com.scy.courseselection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

public class AboutActivity extends MaterialAboutActivity {

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        MaterialAboutCard.Builder appBuilder = new MaterialAboutCard.Builder();
        buildApp(appBuilder, context);
        return new MaterialAboutList(appBuilder.build());

    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return "个人信息";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private void buildApp(MaterialAboutCard.Builder appBuilder, final Context context){
        SharedPreferences sharedPreferences = getSharedPreferences("stu",Context.MODE_PRIVATE);

        appBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .desc("学号")
                .icon(R.drawable.ic_menu_person)
                .text(sharedPreferences.getString("no","what"))
                .build());
        appBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .desc("姓名")
                .text(sharedPreferences.getString("name","what"))
                .icon(R.drawable.ic_menu_person)
                .build());
        appBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .desc("性别")
                .text(sharedPreferences.getString("sex","what"))
                .icon(R.drawable.ic_menu_person)
                .build());
        appBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .desc("年龄")
                .text(sharedPreferences.getString("age","what"))
                .icon(R.drawable.ic_menu_person)
                .build());
        appBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .desc("专业")
                .text(sharedPreferences.getString("dept","what"))
                .icon(R.drawable.ic_menu_person)
                .build());
    }
}
