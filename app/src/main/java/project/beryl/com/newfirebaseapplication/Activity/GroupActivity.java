package project.beryl.com.newfirebaseapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import project.beryl.com.newfirebaseapplication.R;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView createGroup;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_group);
        initView();
        initialpageSetup();
        createGroup.setOnClickListener(this);
        rootLayout.setOnClickListener(this);
    }

    private void initialpageSetup() {
    }

    private void initView() {
        createGroup=(RecyclerView) findViewById(R.id.rv_group_list);
        rootLayout= (RelativeLayout) findViewById(R.id.root_layout);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.root_layout:
                startActivity(new Intent(GroupActivity.this,CreateGroupActivity.class));
                break;

        }
    }
}
