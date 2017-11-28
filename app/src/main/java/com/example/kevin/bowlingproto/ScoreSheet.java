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
    enum FrameState { Incomplete, Open, Closed}

    //enumeration describing the different types of throws.
    enum ThrowType { Split, Strike, Spare, Foul, Gutter, None, NotThrown }

    public Game m_game;

    private TextView m_frameNumber;
    private TextView m_scoreSoFar;

    private Button m_previousFrame;
    private Button m_nextFrame;

    private class Game
    {
        //a vector of our frames
        private Vector<Frame> m_frames;

        //the index in m_frames of the current frame we're on.
        private int m_currentFrame;

        //move to the previous frame, and return it.
        public Frame PreviousFrame()
        {
            //decrement to the previous frame if there is one.
            if(m_currentFrame > 0)
                --m_currentFrame;

            return m_frames.elementAt(m_currentFrame);
        }

        //move to the next frame, and return it.
        public Frame NextFrame()
        {
            //increment to the next frame if their is one
            if(m_currentFrame < 9)
                ++m_currentFrame;

            return m_frames.elementAt(m_currentFrame);
        }

        //get the frame that is currently being displayed
        public Frame GetCurrentFrame(){ return m_frames.elementAt(m_currentFrame); }

        /**
         * Get the total score of the game up until this frame's score
         *
         * @param frame the frame to get the total score for
         * @return the total score for the frame.
         */
        public int GetTotalScoreToFrame(Frame frame)
        {
            int totalScore = 0;

            return totalScore;
        }

        //constructor for our game.
        public Game()
        {
            //initialize our frames
            m_frames = new Vector<Frame>();
            for(int i = 0; i < 9; ++i)
            {
                //initialize all frames with their respective frameNumbers, 1-9
                m_frames.add(new Frame(this,i+1));
            }
            m_frames.add(new TenthFrame(this));

            //the frame we start on is the first frame, located at index 0.
            m_currentFrame = 0;
        }

        public void UpdateScores() {
            Frame frameToUpdate;
            for(int i = 0; i < 9; ++i)
            {
                frameToUpdate = m_frames.elementAt(i);

                if(frameToUpdate.firstThrow.throwType == ThrowType.Strike)
                {

                }
                else if(frameToUpdate.secondThrow.throwType == ThrowType.Spare)
                {

                }
            }
        }

        //class that describes a frame of bowling
        private class Frame
        {
            //array of pins to tell us whether they are standing or knocked down
            private boolean[] pins;

            //the game this frame is in.
            Game m_game;

            //the current state of the frame.
            protected FrameState frameState;

            //the number of this frame
            protected int frameNumber;

            //constructor for a single frame.
            public Frame(Game game,int frame)
            {
                m_game = game;

                //initialize the frameNumber
                frameNumber = frame;

                //set our current frame state to incomplete.
                frameState = FrameState.Incomplete;

                pins = new boolean[10];

                //initialize all pins as standing
                for(int i = 0; i < pins.length;++i)
                    pins[i] = true;
            }
        }

        //class that inheirits from Frame, describes the 10th frame
        private class TenthFrame extends Frame
        {

            //constructor for 10th frame
            public TenthFrame(Game game)
            {
                //construct the frame as the 10th & initilize the 3rd throw.
                super(game,10);
            }
        }
    }

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

    public void onPreviousFrame(View view)
    {
        //setup the new frame
        SetupUI(m_game.PreviousFrame());
    }

    public void onNextFrame(View view)
    {
        //setup the new frame
        SetupUI(m_game.NextFrame());
    }

    //sets up the UI based on the frame we're on
    private void SetupUI(Game.Frame frame)
    {
        //set our frame number text view
        m_frameNumber.setText("Frame " + frame.frameNumber);

        //setting our score so far for the game up until this point.
        m_scoreSoFar.setText(m_game.GetTotalScoreToFrame(frame));
        //if our frame number is one, then we cannot go to a previous frame.
        if(frame.frameNumber == 1)
        {
            m_previousFrame.setEnabled(false);
        }
        else if(frame.frameNumber == 10) //if frame number is 10, cannot go to next frame.
        {
            m_nextFrame.setEnabled(false);
        }
        else //otherwise, we are can go back and forth on frames and should re-enable those butons.
        {
            m_nextFrame.setEnabled(true);
            m_previousFrame.setEnabled(true);
        }

        SetPins();

    }

    private void SetPins()
    {
        Game.Frame frame = m_game.GetCurrentFrame();

        //pins are enabled if the frame isn't done yet
        boolean pinEnabled = (frame.frameState == FrameState.Incomplete);

        SetPin(frame.pins[0],onePin,pinEnabled);
        SetPin(frame.pins[1],twoPin,pinEnabled);
        SetPin(frame.pins[2],threePin,pinEnabled);
        SetPin(frame.pins[3],fourPin,pinEnabled);
        SetPin(frame.pins[4],fivePin,pinEnabled);
        SetPin(frame.pins[5],sixPin,pinEnabled);
        SetPin(frame.pins[6],sevenPin,pinEnabled);
        SetPin(frame.pins[7],eightPin,pinEnabled);
        SetPin(frame.pins[8],ninePin,pinEnabled);
        SetPin(frame.pins[9],tenPin,pinEnabled);
    }

    private void SetPin(boolean pinStanding, ImageButton pinmage,boolean enabled)
    {
        //reset pin filter
        pinmage.clearColorFilter();

        //if the pin isn't standing, mark it as not standing
        if(!pinStanding)
            pinmage.setColorFilter(Color.GREEN);

        pinmage.setEnabled(enabled);
    }

    public void pinClicked(View view)
    {
        switch (view)
        {
            case onePin:
                break;
        }
    }

    private void ChangePin(int pin)
    {

    }
}