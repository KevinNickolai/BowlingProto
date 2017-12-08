package com.example.kevin.bowlingproto;
        import android.graphics.Color;
        import android.graphics.PorterDuff;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import java.util.Vector;

/**
 * ScoreSheet is an activity that represents a single 10 frame game of bowling
 */
public class ScoreSheet extends AppCompatActivity {

    //number of frames in a game
    public final int NUMBER_OF_FRAMES = 10;

    //image buttons for our ten pins
    private ImageButton onePin, twoPin, threePin, fourPin, fivePin,
                        sixPin, sevenPin, eightPin, ninePin, tenPin;

    //enumeration describing the frame's different states.
    enum FrameState {None, Open, Strike, Spare}

    //the game we're currently playing
    public Game m_game;

    //frame information TextViews
    private TextView m_frameNumber;
    private TextView m_scoreSoFar;

    //Navigation and Indication buttons for users to manage their game
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

        /**
         * Updates the score of the previous frame(s), in which we check if the previous frame(s)
         * are spares or strikes, and add a score of this frame according to the previous frame(s)
         */
        private void UpdatePreviousFrameScores()
        {

            //checking if a previous frame index exists
            if((m_currentFrame - 1) >= 0)
            {
                Frame previousFrame = m_frames.elementAt(m_currentFrame-1);

                //a switch statement on the state of the previous frame: checking for spare or strike
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

            //go through every frame up to the frame parameter we passed in.
            for(int i = 0; i < frame.frameNumber; ++i)
            {
                //score is known for this frame, add its score to the total
                if(m_frames.elementAt(i).scoreKnown)
                    totalScore += m_frames.elementAt(i).frameScore;
                else //since the score isn't known for a frame, it can't be known for frames after it.
                    return -1;
            }

            return totalScore;
        }

        //constructor for our game.
        private Game()
        {
            //initialize our frames
            m_frames = new Vector<>();

            //initialize the first 9 frames sequentially, then the 10th frame.
            for(int i = 0; i < NUMBER_OF_FRAMES - 1; ++i)
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

            //this flag indicates whether or not this frame has been completely finished;
            //"complete" means that a frame has been bowled AND The user has clicked the
            //next frame button on the UI, indicating they are done with the frame.
            private boolean complete;

            //The first and second throw # of pins knocked down
            protected int firstThrow, secondThrow;

            //flag to tell us which throw we're on, first or second
            protected boolean isFirstThrow;

            //is our score known for the frame? i.e. is the frame complete and
            //are subsequent frame scores known for spare and strike frames
            private boolean scoreKnown;

            //the score of our frame.
            protected int frameScore;

            //the game this frame is in.
            Game m_game;

            //the current state of the frame.
            protected FrameState frameState;

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
                frameScore = 0;
                for(int i = 0; i < pins.length;++i)
                    pins[i] = true;
            }

            /**
             * Sweeps pins out of the frame
             */
            protected void SweepPins() {

                //score to keep track of how many pins we've knocked down
                int score = 0;

                //flag to tell us whether or not we have pins left standing
                boolean noPinsStanding = true;

                //count the pins knocked down
                for(boolean pinStanding : pins){

                    //if a pin is standing, then this can't be a strike or spare
                    if (pinStanding)
                        noPinsStanding = false;
                    else //pin knocked down, add it to the score
                        ++score;
                }

                //depending on which throw we're on, update the score
                if (isFirstThrow)
                    firstThrow = score;
                else
                    secondThrow = score - firstThrow;

                //checking for strike or spare
                if(noPinsStanding)
                {
                    if (isFirstThrow)
                    {
                        isFirstThrow = false;
                        frameState = FrameState.Strike;
                    }
                    else
                        frameState = FrameState.Spare;

                    //no pins standing, frameScore is 10 no matter what.
                    frameScore = 10;
                }
                else //no strike or spare
                {
                    if(isFirstThrow)
                        isFirstThrow = false;
                    else if(frameState == FrameState.None) // making sure we don't already have a framestate applied in 10th frame.
                    {
                        frameState = FrameState.Open;
                        scoreKnown = true;
                        frameScore = firstThrow + secondThrow;
                    }
                }

                //set the pins for this frame back down.
                SetPins(this);
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
                extraThrow = false;
                thirdThrow = 0;
                isSecondThrow = false;
                super.ResetFrame();
            }

            /**
             * Sweep the pins in the 10th; possibly set more pins up if necessary
             */
            @Override
            protected void SweepPins()
            {
                if(isFirstThrow)
                {
                    super.SweepPins();
                    if(frameState == FrameState.Strike)
                    {
                        extraThrow = true;

                        //set the pins back up in the UI; two more throws to do.
                        for (int i = 0; i < pins.length; ++i)
                            pins[i] = true;

                        SetPins(this);
                    }
                    isSecondThrow = true;
                }
                else if(isSecondThrow)
                {
                    if(frameState != FrameState.Strike)
                    {
                        super.SweepPins();
                        if (frameState == FrameState.Spare)
                        {
                            extraThrow = true;
                            for (int i = 0; i < pins.length; ++i)
                                pins[i] = true;

                            SetPins(this);
                        }
                    }
                    else
                    {
                        boolean noPinsStanding = true;
                        //strike before in the 10th, now just need to count the next two throws' pins
                        for(boolean pinStanding : pins)
                        {
                            if(!pinStanding)
                                ++secondThrow;
                            else
                                noPinsStanding = false;
                        }

                        if(noPinsStanding)
                        {
                            for (int i = 0; i < pins.length; ++i)
                                pins[i] = true;

                            SetPins(this);

                        }
                    }
                    isSecondThrow = false;
                }
                else //third throw, just count the pins for the score
                {
                    for(boolean pinStanding : pins)
                        if(!pinStanding)
                            ++thirdThrow;

                    frameScore += thirdThrow;
                }
            }

            //constructor for 10th frame
            private TenthFrame(Game game)
            {
                //construct the frame as the 10th & initialize the 3rd throw.
                super(game,10);

                //can have an extra throw on the 10th frame, not by default though; need spare or strike in frame.
                extraThrow = false;

                //initialize the score for that third throw as 0.
                thirdThrow = 0;

                //we do not start on the second throw
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
        //associating all TextViews
        m_frameNumber = findViewById(R.id.textViewFrameNumber);
        m_scoreSoFar = findViewById(R.id.textViewFrameTotalScore);

        //associating all Buttons
        m_previousFrame = findViewById(R.id.buttonPreviousFrame);
        m_nextFrame = findViewById(R.id.buttonNextFrame);
        m_resetFrame = findViewById(R.id.buttonResetFrame);
        m_nextThrow = findViewById(R.id.buttonNextThrow);

        //associate all ImageButtons to its set pin.
        onePin = findViewById(R.id.imgBtnOnePin);
        twoPin = findViewById(R.id.imgBtnTwoPin);
        threePin = findViewById(R.id.imgBtnThreePin);
        fourPin = findViewById(R.id.imgBtnFourPin);
        fivePin = findViewById(R.id.imgBtnFivePin);
        sixPin = findViewById(R.id.imgBtnSixPin);
        sevenPin = findViewById(R.id.imgBtnSevenPin);
        eightPin = findViewById(R.id.imgBtnEightPin);
        ninePin = findViewById(R.id.imgBtnNinePin);
        tenPin = findViewById(R.id.imgBtnTenPin);
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
        frame.SweepPins();

        //if the frame is finished
        if(frame.frameState != FrameState.None)
        {
            //any frame other than the 10th
            if(frame.frameNumber != 10)
            {
                m_nextFrame.setEnabled(true);

                m_nextThrow.setEnabled(false);
            }
            //we're on the 10th frame, if we have an extra throw and we aren't on the second throw there are no more throws after
            else if(((Game.TenthFrame)frame).extraThrow && !((Game.TenthFrame)frame).isSecondThrow)
            {
                m_nextFrame.setEnabled(true);

                m_nextThrow.setEnabled(false);
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

        //m_nextFrame should be enabled on setup only if the frame is complete.
        m_nextFrame.setEnabled(frame.complete);

        //m_resetFrame should be enabled on setup only if the frame is not complete.
        m_resetFrame.setEnabled(!frame.complete);

        //disable m_previousFrame on the first frame only.
        m_previousFrame.setEnabled(frame.frameNumber != 1);

        //enable the next throw button if we're on the first throw of the frame.
        m_nextThrow.setEnabled(frame.isFirstThrow);

        //setting score
        int score = m_game.GetTotalScoreToFrame(frame);

        //score not yet known
        if(score == -1)
            m_scoreSoFar.setText("");
        else    //score known, set the textView for score
            m_scoreSoFar.setText(Integer.toString(score));

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
        //disable the ImageButton of the pin
        pinmage.setEnabled(enabled);

        //if the pin isn't standing, mark it as not standing
        if (!pinStanding)
            pinmage.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        else if(!enabled)
            pinmage.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
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