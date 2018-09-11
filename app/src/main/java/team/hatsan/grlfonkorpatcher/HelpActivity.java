package team.hatsan.grlfonkorpatcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by KNH on 2018-02-06.
 */

public class HelpActivity extends AppCompatActivity {

    ViewPager viewPager;
    int currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        final int[] fragments_title = {R.string.helpTitle_text, R.string.helpTitle_gui, R.string.helpTitle_addon};

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        currentFragment = intent.getIntExtra("page", 0);

        viewPager = (ViewPager)findViewById(R.id.help_pager);
        final CustomAdapter adapter = new CustomAdapter(getLayoutInflater(), currentFragment);
        viewPager.setAdapter(adapter);
        getSupportActionBar().setTitle(getString(fragments_title[currentFragment]) + " (1/" + Integer.toString(adapter.getCount()) + ")");
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setTitle(getString(fragments_title[currentFragment]) + " (" + Integer.toString(position + 1) + "/" + Integer.toString(adapter.getCount()) + ")");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_help, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int pos = viewPager.getCurrentItem();
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_help_forward:
                if(pos + 1 > viewPager.getAdapter().getCount() - 1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_lastPage), Toast.LENGTH_SHORT).show();
                }
                else {
                    viewPager.setCurrentItem(pos + 1, true);
                }
                return true;
            case R.id.action_help_back:
                if(pos - 1 < 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_firstPage), Toast.LENGTH_SHORT).show();
                }
                else {
                    viewPager.setCurrentItem(pos - 1, true);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
