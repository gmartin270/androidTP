package com.android.guille.tp6.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.android.guille.tp6.R;
import com.android.guille.tp6.activity.MainActivity;
import com.android.guille.tp6.adapter.UserAdapter;

import org.json.JSONObject;

public class ListFragment extends Fragment {

    static UserAdapter adapter = null;
    View root;

    public ListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(adapter==null) {
            adapter = UserAdapter.getInstance();
        }
        root = inflater.inflate(R.layout.fragment_list, container, false);
        Button fab = (Button) root.findViewById(R.id.btnAdd);
        if(((MainActivity)getActivity()).getIsPort()) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        ((MainActivity) getActivity()).selectUser(null);
                    }catch (Exception e){

                    }
                }
            });
        } else {
            fab.setVisibility(View.INVISIBLE);
        }

        ListView list = (ListView) root.findViewById(R.id.listaPersonas);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONObject jUsr = adapter.getItem(position);
                    ((MainActivity) getActivity()).selectUser(jUsr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return root;
    }
}
