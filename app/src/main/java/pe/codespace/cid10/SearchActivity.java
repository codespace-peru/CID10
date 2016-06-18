package pe.codespace.cid10;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SQLiteHelperCID10 myDBHelper;
    String searchText;
    SearchView searchView;
    MenuItem menuItem;
    ListView myList;
    String[][] resultados;
    AdapterListView myListAdapter;
    Tools.RowCategoria rowCategoria;
    List<Tools.RowCategoria> myListCategorias = new ArrayList<>();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.search_title);
        }

        Intent intent = getIntent();
        searchText = intent.getExtras().getString("searchText");
        TextView textView = (TextView) findViewById(R.id.tvResultados);
        myDBHelper = SQLiteHelperCID10.getInstance(this);
        resultados = myDBHelper.searchTexto(searchText);
        switch (resultados.length){
            case 0:
                textView.setText(Html.fromHtml(getResources().getString(R.string.sin_ocurrencias) + " <b><i>'" + searchText + "'</i></b>"));
                break;
            case 1:
                textView.setText(Html.fromHtml(getResources().getString(R.string.una_ocurrencia) +  " <b><i>'" + searchText + "'</i></b>"));
                break;
            default:
                textView.setText(Html.fromHtml(getResources().getString(R.string.show_ocurrencias1) + " " + resultados.length + " " + getResources().getString(R.string.show_ocurrencias2) + " <b><i>'" + searchText + "'</i></b>"));
                break;
        }

        for (String[] resultado : resultados) {
            rowCategoria = new Tools.RowCategoria(Integer.parseInt(resultado[0]), Integer.parseInt(resultado[1]), resultado[2], resultado[3], Integer.parseInt(resultado[4]));
            myListCategorias.add(rowCategoria);
        }
        myList = (ListView) findViewById(R.id.lvSearchText);
        myListAdapter = new AdapterListView(this,myListCategorias);
        myList.setAdapter(myListAdapter);

        //Agregar el adView
        AdView adView = (AdView)this.findViewById(R.id.adViewSearch);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //Analytics
        Tracker tracker = ((AnalyticsApplication)  getApplication()).getTracker(AnalyticsApplication.TrackerName.APP_TRACKER);
        String nameActivity = getApplicationContext().getPackageName() + "." + this.getClass().getSimpleName();
        tracker.setScreenName(nameActivity);
        tracker.enableAdvertisingIdCollection(true);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MyValues.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0){
                Intent intent = new Intent(this,SearchActivity.class);
                intent.putExtra("searchText", matches.get(0).toString());
                this.startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getResources().getString(R.string.action_search) + "...");
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                searchItem.collapseActionView();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuItem = item;
        int id = item.getItemId();
        switch (id){
            case R.id.action_voice:
                SpeechRecognitionHelper.run(this);
                break;
            case R.id.action_favorites:
                Tools.MostrarFavoritos(this);
                break;
            case R.id.action_share:
                Tools.ShareApp(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.finish();
        Tools.QuerySubmit(this, menuItem, query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }


}
