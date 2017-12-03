package com.example.kevin.bowlingproto;

        import android.graphics.Color;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import java.util.Vector;

public class ScoreSheet extends AppCompatActivity {

    //image buttons for our ten pins
    private ImageButton onePin, twoPin, threePin, fourPin, fivePin,
                        sixPin, sevenPin, eightPin, ninePin, tenPin;


    //enumeration describing the frame's different states.
    enum FrameState {None, Open, Strike, Spare}

    //the game we're currently playing
    public Game m_game;

    //frame information
    private TextView m_frameNumber;
    private TextView m_scoreSoFar;

    //buttons for navigation around the game
    private Button m_previousFrame;
    private Button m_nextFrame;
    private Button m_resetFrame;
    private Button m_nextThrow;

    /**
     * Class that describes one game of bowling
     */
    private class Game
    {
        //a vector of our frames
        private Vector<Frame> m_frames;

        //the index in m_frames of the current frame we're on.
        private int m_currentFrame;

        /**
         * back up to previous frame
         * @return the frame previous to the current one.
         */
        private Frame PreviousFrame()
        {
            //decrement to the previous frame if there is one.
            if(m_currentFrame > 0)
                --m_currentFrame;

            return m_frames.elementAt(m_currentFrame);
        }

        /**
         * move to the next frame
         * @return the frame after the current one
         */
        private Frame NextFrame()
        {
            //increment to the next frame if there is one
            if(m_currentFrame < 9)
                ++m_currentFrame;

            return m_frames.elementAt(m_currentFrame);
        }

        /**
         * Get the currently played frame
         * @return the current frame
         */
        private Frame GetCurrentFrame(){ return m_frames.elementAt(m_currentFrame); }

        private void UpdatePreviousFrameScores()
        {

            //checking if a previous frame index exists
            if((m_currentFrame - 1) >= 0)
            {
                Frame previousFrame = m_frames.elementAt(m_currentFrame-1);
                switch(previousFrame.frameState)
                {
                    //if the previous frame was a spare, then add this frame's first throw to that spare
                    case Spare:
                        previousFrame.frameScore += GetCurrentFrame().firstThrow;
                        previousFrame.scoreKnown = true;
                        break;

                    //if the previous frame was a strike, there are a few different checks for an updated score for it.
                    case Strike:

                        //we are guarenteed to add the current frame first throw if the previous frame was a strike
                        previousFrame.frameScore += GetCurrentFrame().firstThrow;

                        //if the current frame wasn't a strike, we have a second throw to add to the previous
                        if(GetCurrentFrame().frameState != FrameState.Strike)
                        {
                            previousFrame.frameScore += GetCurrentFrame().secondThrow;

                            //since we know this frame was two throws, the previous frame strike score is known.
                            previousFrame.scoreKnown = true;
                        }
                        //we don't set the previousFrame.scoreKnown to true if the current frame was a strike;
                        //since both are strikes, there is another throw we need to account for, when considering the previous frame score.


                        //in the case of a previous frame strike, we need to check the frame before that as well for a strike.
                        if((m_currentFrame - 2) >= 0) //does that frame exist?
                        {
                            //frame exists, check it for strike.
                            Frame beforePreviousFrame = m_frames.elementAt(m_currentFrame - 2);
                            if(beforePreviousFrame.frameState == FrameState.Strike)
                            {
                                //if that frame was a strike, we add the first throw of the current frame
                                beforePreviousFrame.frameScore += GetCurrentFrame().firstThrow;

                                //the score is now known
                                beforePreviousFrame.scoreKnown = true;
                            }
                        }
                }
            }
        }

        /**
         * Get the total score of the game up until this frame's score
         *
         * @param frame the frame to get the total score for
         * @return the total score for the frame.
         */
        private int GetTotalScoreToFrame(Frame frame)
        {
            int totalScore = 0;

            for(int i = 0; i < frame.frameNumber; ++i)
            {
                //score is known for this frame, add its score to the total
                if(m_frames.elementAt(i).scoreKnown)
                {
                    totalScore += m_frames.elementAt(i).frameScore;
                }
                else //since the score isn't known for a frame, it can't be known for frames after it.
                {
                    return -1;
                }
            }

            return totalScore;
        }

        //constructor for our game.
        private Game()
        {
            //initialize our frames
            m_frames = new Vector<>();

            for(int i = 0; i < 9; ++i)
            {
                //initialize all frames with their respective frameNumbers, 1-9
                m_frames.add(new Frame(this,i+1));
            }

            m_frames.add(new TenthFrame(this));

            //the frame we start on is the first frame, located at index 0.
            m_currentFrame = 0;
        }

        //class that describes a frame of bowling
        private class Frame
        {
            //array of pins to tell us whether they are standing or knocked down
            protected boolean[] pins;

            private boolean complete;

            private int firstThrow, secondThrow;
            protected boolean isFirstThrow;
            private boolean scoreKnown;
            private int frameScore;

            //the game this frame is in.
            Game m_game;

            //the current state of the frame.
            private FrameState frameState;

            //the number of this frame
            private int frameNumber;

            /**
             * Constructor for a frame
             * @param game The game this frame is a part of
             * @param frame the number of the frame.
             */
            private Frame(Game game,int frame)
            {
                m_game = game;

                //initialize the frameNumber
                frameNumber = frame;

                //set our current frame state to incomplete.
                frameState = FrameState.None;

                //start on the first throw
                isFirstThrow = true;

                //our first two throws are 0
                firstThrow = secondThrow = 0;

                //the score for this frame isn't known yet
                scoreKnown = false;

                //frame is currently incomplete.
                complete = false;

                //initialize all pins as standing
                pins = new boolean[10];
                for(int i = 0; i < pins.length;++i)
                    pins[i] = true;
            }

            /**
             * Reset the elements of the frame
             */
            protected void ResetFrame()
            {
                isFirstThrow = true;
                firstThrow = 0;
                secondThrow = 0;
                frameState = FrameState.None;
                for(int i = 0; i < pins.length;++i)
                    pins[i] = true;
            }

            /**
             * Sweeps pins out of the frame
             * @return true if no pins are standing
             */
            protected boolean SweepPins()
            {
                // return flag, if no pins are standing then indicate that
                boolean noPinsStanding = true;

                int score = 0;

                for(int i = 0; i < pins.length;++i)
                {
                    //if a pin is standing, then this can't be a strike or spare
                    if(pins[i])
                    {
                        noPinsStanding = false;
                    }
                    else //pin knocked down, add it to the score
                    {
                        ++score;
                    }
                }

                if(isFirstThrow)
                    firstThrow = score;
                else
                    secondThrow = score;

                //strike or spare
                if(noPinsStanding)
                {
                    if (isFirstThrow)
                    {
                        isFirstThrow = false;
                        frameState = FrameState.Strike;
                    }
                    else
                        frameState = FrameState.Spare;
                }
                else
                {
                    if(isFirstThrow)
                        isFirstThrow = false;
                    else
                        frameState = FrameState.Open;
                }

                //set the pins for this frame back down.
                SetPins(this);

                return noPinsStanding;
            }
        }

        //class that inheirits from Frame, describes the 10th frame
        private class TenthFrame extends Frame
        {
            //flag for the extra throw in the frame.
            private boolean extraThrow;

            //flag to tell us if we're on the second throw
            private boolean isSecondThrow;

            //third throw score
            private int thirdThrow;

            /**
             * Reset the current frame; 10th frame requires extra reset steps
             */
            @Override
            protected void ResetFrame()
            {
                super.ResetFrame();
                extraThrow = false;
                thirdThrow = 0;
                isSecondThrow = false;
            }

            /**
             * Sweep the pins in the 10th; possibly set more pins up if necessary
             * @return
             */
            @Override
            protected boolean SweepPins()
            {
                if(isFirstThrow)
                {
                    if(super.SweepPins())
                        extraThrow = true;

                    //set the pins back up in the UI; two more throws to do.
                    for(int i = 0; i < pins.length; ++i)
                        pins[i] = true;

                    SetPins(this);
                    isSecondThrow = true;
                }
                else if(isSecondThrow)
                {
                    if(super.SweepPins())
                        extraThrow = true;

                    for(int i = 0; i < pins.length; ++i)
                        pins[i] = true;

                    SetPins(this);
                    isSecondThrow = false;
                }
                else //third throw, just count the pins for the score
                {
                    for(int i = 0; i < pins.length; ++i)
                    {
                        //if a pin isn't standing, score it
                        if(!pins[i])
                            ++thirdThrow;
                    }
                }

                //we don't check for pins standing in 10th; game's over after third throw pin sweep
                return false;
            }

            //constructor for 10th frame
            private TenthFrame(Game game)
            {
                //construct the frame as the 10th & initilize the 3rd throw.
                super(game,10);

                //can have an extra throw on the 10th frame, not by default though; need spare or strike in frame.
                extraThrow = false;

                //initialize the score for that third throw as 0.
                thirdThrow = 0;

                isSecondThrow = false;
            }
        }
    }

    /**
     * Function called on instantiation of this activity
     * @param savedInstanceState a previously saved instance of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_sheet);

        //initialize the game
        m_game = new Game();

        //associate all views.
        InitializeViews();

        //setup the UI to our current frame.
        SetupUI(m_game.GetCurrentFrame());
    }

    /**
     * Initializes all views for this activity
     */
    public void InitializeViews()
    {
        //associating all of our views
        m_frameNumber = (TextView)findViewById(R.id.textViewFrameNumber);
        m_scoreSoFar = (TextView)findViewById(R.id.textViewFrameTotalScore);
        m_previousFrame = (Button)findViewById(R.id.buttonPreviousFrame);
        m_nextFrame = (Button)findViewById(R.id.buttonNextFrame);
        m_resetFrame = (Button)findViewById(R.id.buttonResetFrame);
        m_nextThrow = (Button)findViewById(R.id.buttonNextThrow);

        //initialize all ImageButtons to a set pin
        onePin = (ImageButton)findViewById(R.id.imgBtnOnePin);
        twoPin = (ImageButton)findViewById(R.id.imgBtnTwoPin);
        threePin = (ImageButton)findViewById(R.id.imgBtnThreePin);
        fourPin = (ImageButton)findViewById(R.id.imgBtnFourPin);
        fivePin = (ImageButton)findViewById(R.id.imgBtnFivePin);
        sixPin = (ImageButton)findViewById(R.id.imgBtnSixPin);
        sevenPin = (ImageButton)findViewById(R.id.imgBtnSevenPin);
        eightPin = (ImageButton)findViewById(R.id.imgBtnEightPin);
        ninePin = (ImageButton)findViewById(R.id.imgBtnNinePin);
        tenPin = (ImageButton)findViewById(R.id.imgBtnTenPin);
    }

    /**
     * Displays the previous frame in the game.
     * @param view the view clicked to cause this event
     */
    public void onPreviousFrame(View view)
    {
        //setup the new frame
        SetupUI(m_game.PreviousFrame());
    }

    /**
     * Displays the next frame in the game.
     * @param view the view clicked to cause this event
     */
    public void onNextFrame(View view)
    {
        //if this frame isn't currently complete and next frame is clicked, this frame is complete.
        if(!m_game.GetCurrentFrame().complete)
        {
            m_game.GetCurrentFrame().complete = true;
            m_game.UpdatePreviousFrameScores();
        }

        //setup the new frame
        SetupUI(m_game.NextFrame());
    }

    /**
     * Reset the current frame being bowled.
     * @param view the view which called this function
     */
    public void onResetFrame(View view)
    {
        Game.Frame frame = m_game.GetCurrentFrame();
        frame.ResetFrame();
        SetupUI(frame);
    }

    /**
     * Moves the frame to the next throw
     * @param view the view that called this function
     */
    public void onNextThrow(View view)
    {
        Game.Frame frame = m_game.GetCurrentFrame();

        //sweep pins if this throw is complete
        boolean noPinsStanding = frame.SweepPins();

        //if the frame is finished
        if(frame.frameState != FrameState.None)
        {
            SetupUI(m_game.NextFrame());
        }
        else if(frame.frameNumber != 10) //second throw on any frame other than 10th
        {
            //enable next frame button
            m_nextFrame.setEnabled(true);

            //disable next throw button
            m_nextThrow.setEnabled(false);
        }
        else //on the 10th frame, need to check for extra throws
        {

            if(!((Game.TenthFrame)frame).isSecondThrow)
            {

            }
        }
    }

    /**
     * Setup UI based on a frame
     * @param frame to set the UI for
     */
    private void SetupUI(Game.Frame frame)
    {
        //set our frame number text view
        m_frameNumber.setText("Frame " + frame.frameNumber);

        //enable the next frame button if this frame is already complete; disable if incomplete.
        m_nextFrame.setEnabled(frame.complete);

        //enable the reset frame button only if the frame is incomplete.
        m_resetFrame.setEnabled(!frame.complete);

        //on the first frame, there are no other frames to go back to; disable previous frame button on the first frame only.
        m_previousFrame.setEnabled(frame.frameNumber != 1);

        //enable the next throw button if we're on the first throw of the frame.
        m_nextThrow.setEnabled(frame.isFirstThrow);

        //setting score
        int score = m_game.GetTotalScoreToFrame(m_game.GetCurrentFrame());

        //score not yet known
        if(score == -1)
        {
            m_scoreSoFar.setText("");
        }
        else
        {
            m_scoreSoFar.setText(score);
        }

        //set the pins for this frame.
        SetPins(frame);
    }

    /**
     * Set the pins up on the UI
     * @param frame the frame we want to display pins for
     */
    private void SetPins(Game.Frame frame)
    {
        //if the frame is complete, no pins should be enabled.
        if(frame.complete)
        {
            SetPin(frame.pins[0], onePin, false);
            SetPin(frame.pins[1], twoPin, false);
            SetPin(frame.pins[2], threePin, false);
            SetPin(frame.pins[3], fourPin, false);
            SetPin(frame.pins[4], fivePin, false);
            SetPin(frame.pins[5], sixPin, false);
            SetPin(frame.pins[6], sevenPin, false);
            SetPin(frame.pins[7], eightPin, false);
            SetPin(frame.pins[8], ninePin, false);
            SetPin(frame.pins[9], tenPin, false);
        }
        else if(frame.isFirstThrow) //on the first throw, all pins enabled
        {
            //for each pin, if the frame is not complete, then the pin should be enabled.
            SetPin(frame.pins[0], onePin, true);
            SetPin(frame.pins[1], twoPin, true);
            SetPin(frame.pins[2], threePin, true);
            SetPin(frame.pins[3], fourPin, true);
            SetPin(frame.pins[4], fivePin, true);
            SetPin(frame.pins[5], sixPin, true);
            SetPin(frame.pins[6], sevenPin, true);
            SetPin(frame.pins[7], eightPin, true);
            SetPin(frame.pins[8], ninePin, true);
            SetPin(frame.pins[9], tenPin, true);
        }
        else //second throw, pins are only enabled if they are still standing
        {
            SetPin(frame.pins[0], onePin, frame.pins[0]);
            SetPin(frame.pins[1], twoPin, frame.pins[1]);
            SetPin(frame.pins[2], threePin, frame.pins[2]);
            SetPin(frame.pins[3], fourPin, frame.pins[3]);
            SetPin(frame.pins[4], fivePin, frame.pins[4]);
            SetPin(frame.pins[5], sixPin, frame.pins[5]);
            SetPin(frame.pins[6], sevenPin, frame.pins[6]);
            SetPin(frame.pins[7], eightPin, frame.pins[7]);
            SetPin(frame.pins[8], ninePin, frame.pins[8]);
            SetPin(frame.pins[9], tenPin, frame.pins[9]);
        }
    }

    /**
     * Sets a single pin up on the UI
     * @param pinStanding flag to tell us if the pin is standing or not
     * @param pinmage the ImageButton for the pin
     * @param enabled flag on if this pin should be enabled to be interacted with.
     */
    private void SetPin(boolean pinStanding, ImageButton pinmage,boolean enabled)
    {
        //reset pin tint (setColorFilter passed with null will do this)
        pinmage.getDrawable().setColorFilter(null);

        //if the pin isn't standing, mark it as not standing
        if(!pinStanding)
            pinmage.getDrawable().setTint(Color.GREEN);

        pinmage.setEnabled(enabled);
    }

    /**
     * Changes a pin from standing to knocked down or knocked down to standing
     * @param view the view that called this function
     */
    public void ChangePin(View view)
    {
        Game.Frame frame = m_game.GetCurrentFrame();

        int vID = view.getId();
        if(vID == onePin.getId())
        {
            frame.pins[0] = !frame.pins[0];
            SetPin(m_game.GetCurrentFrame().pins[0],onePin,true);
        }
        else if(vID == twoPin.getId())
        {
            frame.pins[1] = !frame.pins[1];
            SetPin(m_game.GetCurrentFrame().pins[1],twoPin,true);
        }
        else if(vID == threePin.getId())
        {
            frame.pins[2] = !frame.pins[2];
            SetPin(m_game.GetCurrentFrame().pins[2],threePin,true);
        }
        else if(vID == fourPin.getId())
        {
            frame.pins[3] = !frame.pins[3];
            SetPin(m_game.GetCurrentFrame().pins[3],fourPin,true);
        }
        else if(vID == fivePin.getId())
        {
            frame.pins[4] = !frame.pins[4];
            SetPin(m_game.GetCurrentFrame().pins[4],fivePin,true);
        }
        else if(vID == sixPin.getId())
        {
            frame.pins[5] = !frame.pins[5];
            SetPin(m_game.GetCurrentFrame().pins[5],sixPin,true);
        }
        else if(vID == sevenPin.getId())
        {
            frame.pins[6] = !frame.pins[6];
            SetPin(m_game.GetCurrentFrame().pins[6],sevenPin,true);
        }
        else if(vID == eightPin.getId())
        {
            frame.pins[7] = !frame.pins[7];
            SetPin(m_game.GetCurrentFrame().pins[7],eightPin,true);
        }
        else if(vID == ninePin.getId())
        {
            frame.pins[8] = !frame.pins[8];
            SetPin(m_game.GetCurrentFrame().pins[8],ninePin,true);
        }
        else if(vID == tenPin.getId())
        {
            frame.pins[9] = !frame.pins[9];
            SetPin(m_game.GetCurrentFrame().pins[9],tenPin,true);
        }
    }
}