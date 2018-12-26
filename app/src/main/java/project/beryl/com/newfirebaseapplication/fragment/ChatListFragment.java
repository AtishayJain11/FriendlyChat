package project.beryl.com.newfirebaseapplication.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import project.beryl.com.newfirebaseapplication.R;

public class ChatListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.chat_list_fragment_4, viewGroup, false);


       /* contact= (FloatingActionButton) view1.findViewById(R.id.fab);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SelectContactPage.class);
                startActivity(intent);

            }
        });*/

        return view1;
    }
}