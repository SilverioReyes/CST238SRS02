# CST238SRS02
Target Alarm Range

Name: Silverio Reyes

Notes: In developing the application, I had to account for 3 input fields that a user can select to set their alarm based on their    temperature ranges such as the minimum, target, and maximum value via Fahrenheit or Celsius. I wanted the application to be interactive with the user so I learned how to implement a SeekBar which is an extension of ProgressBar that adds a draggable thumb. This would allow the users to touch the thumb and drag it either left or right which then sets the current progress level. 

The main nested class used as an interface that is a callback that I used was the on seek bar change listener that gathers and displays the current progress level to the user. I would also change the text color to red if the user's values are not within range; for example, the minimum value cannot exceed the maximum value and the target value must be within the minimum and maximum value set by the user. It would have been beneficial to change the text views to edit views to give the user the option to type in a integer value.

Issues: Since the application requires to display the current state of the values to Fahrenheit or Celsius, the difficult part was setting the offset; values such as [200, 500]F and [90,240]C. I had to set the max less the beginning offset to compensate the min values starting points (200 and 93). Example: 500F – 200F = 300F. Range = [0, 300]F Starting value: 0 + 200F, total range now [200, 500]F.

The other issues addressed is that the application specification range values do not convert properly using the formulas for converting to and from Fahrenheit and Celsius (℃=(℉-32)×5/9) and (℉=℃×(9/5)+32). Converting [200, 500]F to Celsius is approximately [93,260]C. It was noted that the temperatures could have a 5-degree delta value when the user is setting the alarm; the delta in temperature change I set is 1 degree;

The other issue I encountered that is a current bug in my application corrupts the stack on the back pressed event when the user clicks to the previous activity which results in a crash. This only occurs if the user clicks back while the output activity is still running in the background and the user does not reset their set temperature values on the main activity or exits the main activity. After debugging, the error may be occurring due to the fact that the display activity is not being destroyed once the user clicks back because it has not finished. The display activity is also set as a single task instance in order to maintain its current state to display the current temp as it rises and falls and to also maintain the alert dialog messages if the user enters via notification from outside the application. 

Since the display output activity is used for demo purposes, a simple recommendation as to removing the notification and alert dialog messages once the user has clicked back and generate a report to send via text or email. This way nothing is running in the background or is left hanging when the activity has finished.

