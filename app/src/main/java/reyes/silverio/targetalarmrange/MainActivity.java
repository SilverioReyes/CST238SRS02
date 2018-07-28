/*************************************************************
 * Author:          Silverio Reyes
 * Filename:        MainActivity.java
 * Organization:    Oregon Institute of Technology
 * Class:           CST238 GUI
 *
 * Date Created:    7/01/18 - Mitch Besser-Laber Created .gitignore,
 *                            README.md, and REPORT.md files
 *
 * Date Modified:   7/22/18 - Created project and
 *                            added dependencies
 *
 *                  7/23/18 - Designed main activity UI
 *
 *                            Implemented listener function
 *                            for the min, target, and max
 *                            values for the seek bar attributes
 *
 *                  7/24/18 - Implemented input validation for
 *                            user action on slider for seek bar
 *
 *                            Values based on user selection on
 *                            seek bar for min, target, and max
 *                            are now displayed to the UI. Toast
 *                            message is also displayed along with
 *                            handling error for ranges. This
 *                            can be improved by using an
 *                            alert dialog box instead of
 *                            toast message.
 *
 *                            Fixed logic error for when a user
 *                            converts from Celsius to Fahrenheit
 *                            and vice-versa. Since the min values
 *                            do not start at 0, I had to compensate
 *                            for the offset, especially when
 *                            there is a value already set.
 *                            for example, 200 F = 93 C.
 *                            The display was correct but the
 *                            progress bar added 93 to it when
 *                            converting to celsius or 200 when
 *                            converting to fahrenheit.
 *                            Had to subtract the step value
 *                            since it is added within the seekbar
 *                            listener event for both cases when
 *                            setting the progress bar within the
 *                            ToggleTempConverterListener
 *                            method.
 *
 *                            Changed the theme color for the
 *                            progress bar when it is in
 *                            fahrenheit mode or celsius mode
 *                            to distinguish the two. Conversion
 *                            toggle button fully functional.
 *
 *                  7/28/18 - Recommendation: Change text view
 *                            for the temperature value as an
 *                            input to edit view and set regular
 *                            expression to allow user to enter
 *                            an integer value. Progress bar
 *                            must be updated immediately once
 *                            entered by user but make sure its
 *                            valid.
 **************************************************************/
package reyes.silverio.targetalarmrange;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    // Variables instantiated to handle UI resources
    SeekBar seekBarMin, seekBarTarget, seekBarMax;
    TextView tvMinvalue, tvTargetvalue, tvMaxvalue, tvTempMinvalue, tvTempMaxvalue;
    ToggleButton tbTempConverter;
    Button btnSetalarm;

    private boolean minExceedsmax = false;
    private boolean targetWithinRange = true;
    private boolean isFahrenheit = true;

    private String minTemprange = "200\u00B0" + "F";
    private String maxTemprange = "500\u00B0" + "F";
    private String degreeSymbol = "\u00B0";

    private ColorStateList defaultThemeColor;

    public static final String sharedPrefs = "TemperatureSettingPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeVariables();

        // Initialize the text view values for the seek bar to fahrenheit by default
        DefaultIsFahrenheit();

        // Monitor toggle button for temp conversion
        ToggleTempConverterListener();

        // Monitor changes within seek bars
        SeekBarListeners();

        // Validate form
        ValidateSettingAlarmForm();
    }

    private void InitializeVariables() {
        // Variables for the UI elements and connects them to the XML layout
        seekBarMin = (SeekBar)findViewById(R.id.seekBarMin);
        seekBarTarget = (SeekBar)findViewById(R.id.seekBarTarget);
        seekBarMax = (SeekBar)findViewById(R.id.seekBarMax);

        tvMinvalue = (TextView)findViewById(R.id.TVMinvalue);
        tvTargetvalue = (TextView)findViewById(R.id.TVTargetvalue);
        tvMaxvalue = (TextView)findViewById(R.id.TVMaxvalue);
        tvTempMinvalue = (TextView)findViewById(R.id.TVTempMin);
        tvTempMaxvalue = (TextView)findViewById(R.id.TVTempMax);

        tbTempConverter = (ToggleButton)findViewById(R.id.TBTempConverter);
        btnSetalarm = (Button)findViewById(R.id.btnSetalarm);

        // Get the default theme text color for text views
        defaultThemeColor = tvMaxvalue.getTextColors();
    }

    private void SeekBarListeners()
    {
        // Seek bar for minimum value
        seekBarMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int minvalProgress = seekBarMin.getProgress();
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressVal, boolean user) {
                if(isFahrenheit)
                {
                    // Set starting val offset and set the progress drawable color (line and ball) to match toggle button color for Fahrenheit
                    minvalProgress = progressVal + 200;
                    seekBarMin.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.FAHRENHEIT_THEME),PorterDuff.Mode.SRC_IN));
                    seekBarMin.getThumb().setColorFilter(getResources().getColor(R.color.FAHRENHEIT_THEME), PorterDuff.Mode.SRC_IN);
                }
                else{
                    // Set starting val offset and set the progress drawable color (line and ball) to match toggle button color for Celsius
                    minvalProgress = progressVal + 93;
                    seekBarMin.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.CELSIUS_THEME),PorterDuff.Mode.SRC_IN));
                    seekBarMin.getThumb().setColorFilter(getResources().getColor(R.color.CELSIUS_THEME), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Default tracking based on touched gesture by user
                tvMinvalue.setTextColor((defaultThemeColor));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Get the progress value after user has stopped
                if (seekBarMin.getProgress() <= seekBarMax.getProgress())
                {
                    minExceedsmax = false;
                    tvMinvalue.setText(String.valueOf(minvalProgress));
                }
                else{
                    minExceedsmax = true;
                    tvMinvalue.setTextColor(Color.RED);
                    tvMinvalue.setText(String.valueOf(minvalProgress));
                    Toast.makeText(MainActivity.this, "Minimum value cannot exceeed maximum value",
                            Toast.LENGTH_LONG).show();
                }

                if (seekBarMin.getProgress() > seekBarTarget.getProgress())
                {
                    targetWithinRange = false;
                    tvTargetvalue.setTextColor(Color.RED);
                    Toast.makeText(MainActivity.this, "Target value must be within the min and max value range",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    if(seekBarTarget.getProgress() <= seekBarMax.getProgress())
                    {
                        targetWithinRange =true;
                        tvTargetvalue.setTextColor(defaultThemeColor);
                    }
                    else {
                        targetWithinRange = false;
                        tvTargetvalue.setTextColor(Color.RED);
                    }
                }
            }
        });

        // Seek bar for Target value
        seekBarTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int targetvalProgress = seekBarTarget.getProgress();
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressVal, boolean user) {
                if(isFahrenheit)
                {
                    targetvalProgress = progressVal + 200;
                    seekBarTarget.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.FAHRENHEIT_THEME),PorterDuff.Mode.SRC_IN));
                    seekBarTarget.getThumb().setColorFilter(getResources().getColor(R.color.FAHRENHEIT_THEME), PorterDuff.Mode.SRC_IN);
                }
                else{
                    targetvalProgress = progressVal + 93;
                    seekBarTarget.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.CELSIUS_THEME),PorterDuff.Mode.SRC_IN));
                    seekBarTarget.getThumb().setColorFilter(getResources().getColor(R.color.CELSIUS_THEME), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Default tracking based on touched gesture by user
                tvTargetvalue.setTextColor((defaultThemeColor));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Get the progress value after user has stopped
                tvTargetvalue.setText(String.valueOf(targetvalProgress));
                if(seekBarTarget.getProgress() >= seekBarMin.getProgress() && seekBarTarget.getProgress() <= seekBarMax.getProgress())
                {
                    targetWithinRange = true;
                }
                else{
                    targetWithinRange = false;
                    tvTargetvalue.setTextColor(Color.RED);
                    Toast.makeText(MainActivity.this, "Target value must be within the min and max value range",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Seek bar for max value
        seekBarMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int maxvalProgress = seekBarMax.getProgress();
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressVal, boolean user) {
                if(isFahrenheit)
                {
                    maxvalProgress = progressVal + 200;
                    seekBarMax.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.FAHRENHEIT_THEME),PorterDuff.Mode.SRC_IN));
                    seekBarMax.getThumb().setColorFilter(getResources().getColor(R.color.FAHRENHEIT_THEME), PorterDuff.Mode.SRC_IN);
                }
                else{
                    maxvalProgress = progressVal + 93;
                    seekBarMax.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.CELSIUS_THEME),PorterDuff.Mode.SRC_IN));
                    seekBarMax.getThumb().setColorFilter(getResources().getColor(R.color.CELSIUS_THEME), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Default tracking based on touched gesture by user
                tvMinvalue.setTextColor(defaultThemeColor);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Get the progress value after user has stopped
                tvMaxvalue.setText(String.valueOf(maxvalProgress));
                if(seekBarMax.getProgress() >= seekBarMin.getProgress())
                {
                    minExceedsmax = false;
                }
                else {
                    minExceedsmax = true;
                    tvMinvalue.setTextColor(Color.RED);
                    Toast.makeText(MainActivity.this, "Minimum value cannot exceeed maximum value",
                            Toast.LENGTH_LONG).show();
                }

                if (seekBarMax.getProgress() < seekBarTarget.getProgress())
                {
                    targetWithinRange = false;
                    tvTargetvalue.setTextColor(Color.RED);
                    Toast.makeText(MainActivity.this, "Target value must be within the min and max value range",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    if(seekBarMin.getProgress() > seekBarTarget.getProgress()) {
                        targetWithinRange = false;
                        tvTargetvalue.setTextColor(Color.RED);
                    }
                    else {
                        targetWithinRange = true;
                        tvTargetvalue.setTextColor(defaultThemeColor);
                    }
                }
            }
        });
    }

    private void ToggleTempConverterListener()
    {
        tbTempConverter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                isFahrenheit = isChecked;

                // CONVERT CELSIUS TO FAHRENHEIT
                if(isFahrenheit)
                {
                    // Set the temperature display ranges
                    minTemprange = "200\u00B0" + "F";
                    maxTemprange = "500\u00B0" + "F";
                    tvTempMinvalue.setText(minTemprange);
                    tvTempMaxvalue.setText(maxTemprange);

                    // Set the max offset to 300 since starting position is 200 but still mains values from 0 to 500 (200 + 300 = 500)
                    // Parse the text view value to an integer and get the ceiling value after converting from celsius to Fahrenheit
                    // Set the ceiling value and set the progress. Make sure to subtract the offset by 200!
                    seekBarMin.setMax(300);
                    int minVal = Integer.parseInt(tvMinvalue.getText().toString());
                    int ceilingMinVal = (int)Double.parseDouble(String.valueOf(Math.ceil(minVal * 1.8 + 32)));
                    tvMinvalue.setText(String.valueOf(ceilingMinVal));
                    seekBarMin.setProgress((Integer.parseInt(tvMinvalue.getText().toString()))-200);

                    // Target value conversion
                    seekBarTarget.setMax(300);
                    int targetVal = Integer.parseInt(tvTargetvalue.getText().toString());
                    int ceilingTargetVal = (int)Double.parseDouble(String.valueOf(Math.ceil(targetVal * 1.8 + 32)));
                    tvTargetvalue.setText(String.valueOf(ceilingTargetVal));
                    seekBarTarget.setProgress((Integer.parseInt(tvTargetvalue.getText().toString()))-200);

                    // Max value conversion
                    seekBarMax.setMax(300);
                    int maxVal = Integer.parseInt(tvMaxvalue.getText().toString());
                    int ceilingMaxVal = (int)Double.parseDouble(String.valueOf(Math.ceil(maxVal * 1.8 + 32)));
                    tvMaxvalue.setText(String.valueOf(ceilingMaxVal));
                    seekBarMax.setProgress((Integer.parseInt(tvMaxvalue.getText().toString()))-200);
                }

                // CONVERT FAHRENHEIT TO CELSIUS
                else{

                    // Set the temperature display ranges
                    minTemprange = "93\u00B0" + "C";
                    maxTemprange = "260\u00B0" + "C";
                    tvTempMinvalue.setText(minTemprange);
                    tvTempMaxvalue.setText(maxTemprange);

                    // Set the offset max to 167 because of adding 93 as our starting value for zero (167 + 93 = 260)
                    // Convert the string value from text view to an integer value
                    // Now that we have the Fahrenheit value, convert and round to celsius, set to text view
                    // Then set the progress bar to that value. Again, offset is 93 as our starting value so subtract!
                    seekBarMin.setMax(167);
                    int minVal = Integer.parseInt(tvMinvalue.getText().toString());
                    tvMinvalue.setText(String.valueOf(Math.round((minVal - 32)*.5556)));
                    seekBarMin.setProgress((Integer.parseInt(tvMinvalue.getText().toString()))-93);

                    // Target value conversion
                    seekBarTarget.setMax(167);
                    int targetVal = Integer.parseInt(tvTargetvalue.getText().toString());
                    tvTargetvalue.setText(String.valueOf(Math.round((targetVal - 32)*.5556)));
                    seekBarTarget.setProgress((Integer.parseInt(tvTargetvalue.getText().toString()))-93);

                    // Max value conversion
                    seekBarMax.setMax(167);
                    int maxVal = Integer.parseInt(tvMaxvalue.getText().toString());
                    tvMaxvalue.setText(String.valueOf(Math.round((maxVal - 32)*.5556)));
                    seekBarMax.setProgress((Integer.parseInt(tvMaxvalue.getText().toString()))-93);
                }
            }
        });
    }

    private void DefaultIsFahrenheit()
    {
        // Set the temperature display ranges
        tvTempMinvalue.setText(minTemprange);
        tvTempMaxvalue.setText(maxTemprange);

        // Set the default temp values for initial load
        seekBarMin.setProgress(0);
        seekBarMin.setMax(300);
        tvMinvalue.setText(String.valueOf(200));

        seekBarTarget.setProgress(0);
        seekBarTarget.setMax(300);
        tvTargetvalue.setText(String.valueOf(200));

        seekBarMax.setProgress(0);
        seekBarMax.setMax(300);
        tvMaxvalue.setText(String.valueOf(200));
    }

    private void ValidateSettingAlarmForm()
    {
        btnSetalarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if temp value settings are valid before sending to next intent
                // Consider adding a boolean for when user has moved progress bar
                if(!minExceedsmax && targetWithinRange)
                {
                    // Method to handle next activity
                    HandlerForNextIntent();
                }
            }
        });
    }

    private void HandlerForNextIntent() {
        // Get resources from shared preferences
        SharedPreferences preferences = this.getSharedPreferences("TemperatureSettingPreferences", Context.MODE_PRIVATE);

        // Edit and save entry list
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString("MinTempValue", tvMinvalue.getText().toString());
        editor.putString("MaxTempValue", tvMaxvalue.getText().toString());
        editor.putBoolean("IsFahrenheit",isFahrenheit);
        editor.apply();

        // Pass intent to next activity
        Intent CurrentTempDisplay = new Intent(MainActivity.this, DisplayCurrentTemp.class);
        startActivity(CurrentTempDisplay);
    }

    /**********************************************************************
     * Purpose: This function handles the key event for pressing the back
     *          button when inside the main activity.
     *
     * Pre-condition: Back button not pressed. Stay in activity
     *
     * Post-condition: Back button is pressed. Exit application (Clear stack)
     ************************************************************************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
