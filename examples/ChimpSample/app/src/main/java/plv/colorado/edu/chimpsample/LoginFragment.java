package plv.colorado.edu.chimpsample;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Pezh on 4/8/17.
 */

public class LoginFragment extends Fragment {
    View inflatedView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_login, container, false);
        return inflatedView;
    }

    @Override
    public void onResume(){
        super.onResume();

        Button btnLogin = (Button) inflatedView.findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), CountdownActivity.class);
                startActivity(intent);
            }
        });

    }
}
