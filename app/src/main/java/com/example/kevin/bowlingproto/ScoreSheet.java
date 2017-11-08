package com.example.kevin.bowlingproto;


        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import java.util.Vector;
        import android.text.InputFilter;
        import android.text.Spanned;

/**
 * Created by npatel on 4/5/2016.
 */

public class ScoreSheet extends AppCompatActivity {

    //enumeration describing the frame's different states.
    enum FrameState { Incomplete, Open, Closed}

    //enumeration describing the different types of throws.
    enum ThrowType { Split, Strike, Spare, Foul, Gutter, None, NotThrown }

    public Game m_game;

    private TextView m_frameNumber;
    private TextView m_scoreSoFar;
    private EditText m_firstThrow;
    private EditText m_secondThrow;
    private Button m_previousFrame;
    private Button m_nextFrame;

    private ScoreListener m_firstThrowScoreListener;
    private ScoreListener m_secondThrowScoreListener;

    //from https://capdroidandroid.wordpress.com/2016/04/07/set-minimum-maximum-value-in-edittext-android/
    //to limit edittext input to 0-10
    public class MinMaxFilter implements InputFilter {

        private int mIntMin, mIntMax;

        public MinMaxFilter(int minValue, int maxValue) {
            this.mIntMin = minValue;
            this.mIntMax = maxValue;
        }

        public MinMaxFilter(String minValue, String maxValue) {
            this.mIntMin = Integer.parseInt(minValue);
            this.mIntMax = Integer.parseInt(maxValue);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(mIntMin, mIntMax, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

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

            Frame frameToAdd;

            //add the score up until the frame that we're on, or up until the 9th frame.
            for(int i = 0; i < GetCurrentFrame().frameNumber && i < 9; ++i)
            {
                //the score to add from the frame
                frameToAdd = m_frames.elementAt(i);
                if(frameToAdd.frameState != FrameState.Incomplete)
                {

                }
            }

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
            //the game this frame is in.
            Game m_game;

            //the two throws in a frame
            protected Throw firstThrow, secondThrow;

            //the current state of the frame.
            protected FrameState frameState;

            //the number of this frame
            protected int frameNumber;

            //the score of this frame
            protected int frameScore;

            //constructor for a single frame.
            public Frame(Game game,int frame)
            {
                m_game = game;

                //initialize the frameNumber
                frameNumber = frame;

                //initialize the two throws
                firstThrow = new Throw(this, true);
                secondThrow = new Throw(this,false);

                //set our current frame state to incomplete.
                frameState = FrameState.Incomplete;
            }

            public void CloseFrame()
            {
                frameState = FrameState.Closed;

            }

            public void OpenFrame()
            {
                frameState = FrameState.Open;
                frameScore = firstThrow.m_score + secondThrow.m_score;
            }

            //class that describes a single throw in bowling
            protected class Throw
            {
                //the frame we're a part of
                Frame m_frame;

                //flag to indicate if this is the first throw of the frame
                boolean isFirstThrow;

                //the m_score of this throw
                private int m_score;

                //the type of throw this throw is
                private ThrowType throwType;

                public void setScore(int score)
                {
                    m_score = score;

                    /**override this in TenthFrame
                     //checking if we're in the tenth frame
                     if(throwToScore.m_frame.frameNumber == 10)
                     {
                     if(enteredScore == 10)
                     {
                     throwToScore.throwType = ThrowType.Strike;
                     }
                     else if(enteredScore == 0)
                     {
                     throwToScore.throwType = ThrowType.Gutter;
                     }
                     else
                     {
                     throwToScore.throwType = ThrowType.None;
                     }
                     }*/
                    if(isFirstThrow)
                    {
                        if(m_score == 10)
                        {
                            throwType = ThrowType.Strike;
                            m_frame.frameState = FrameState.Closed;
                        }
                        else if(m_score == 0)
                        {
                            throwType = ThrowType.Gutter;
                        }
                        else
                        {
                            throwType = ThrowType.None;
                        }
                    }
                    else //second throw
                    {
                        //is the overall m_score of the two throws of this frame 10
                        if(m_score + m_frame.firstThrow.m_score == 10)
                        {
                            throwType = ThrowType.Spare;
                            m_frame.frameState = FrameState.Closed;
                        }
                        else if(m_score == 0)
                        {
                            throwType = ThrowType.Gutter;
                            m_frame.frameState = FrameState.Open;
                        }
                        else
                        {
                            throwType = ThrowType.None;
                            m_frame.frameState = FrameState.Open;
                        }
                    }
                }

                //constructor for a Throw
                public Throw(Frame frame, boolean firstThrow)
                {
                    //we have no m_score yet.
                    m_score = 0;

                    //this throw was just initilized, and hasn't been thrown yet.
                    throwType = ThrowType.NotThrown;

                    //initialize the flag for first throw.
                    isFirstThrow = firstThrow;
                }
            }
        }

        //class that inheirits from Frame, describes the 10th frame
        private class TenthFrame extends Frame
        {
            //the potential third throw of the tenth frame
            protected Throw thirdThrow;

            //constructor for 10th frame
            public TenthFrame(Game game)
            {
                //construct the frame as the 10th & initilize the 3rd throw.
                super(game,10);
                thirdThrow = new Throw(this,false);
            }
        }
    }

    //using https://stackoverflow.com/questions/8543449/how-to-use-the-textwatcher-class-in-android/42951863#42951863
    //as a reference to create my own listener of the m_score editText elements.
    private class ScoreListener implements TextWatcher{

        //the throw that we are changing the m_score of
        private Game.Frame.Throw throwToScore;

        public void setThrowToScore(Game.Frame.Throw toScore) {throwToScore = toScore;}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int enteredScore = Integer.decode(s.toString());
                throwToScore.setScore(enteredScore);
                m_game.UpdateScores();
            } catch (NumberFormatException e) {
                s.clear();
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

        //setting up the m_score listeners & input filters
        m_firstThrowScoreListener = new ScoreListener();
        m_secondThrowScoreListener = new ScoreListener();

        m_firstThrow.addTextChangedListener(m_firstThrowScoreListener);
        m_firstThrow.setFilters(new InputFilter[]{ new MinMaxFilter("0", "10")});

        m_secondThrow.addTextChangedListener(m_secondThrowScoreListener);
        m_secondThrow.setFilters(new InputFilter[]{ new MinMaxFilter("0", "10")});

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
        m_firstThrow = (EditText)findViewById(R.id.editTextFrameFirstThrow);
        m_secondThrow = (EditText)findViewById(R.id.editTextFrameSecondThrow);
        m_previousFrame = (Button)findViewById(R.id.buttonPreviousFrame);
        m_nextFrame = (Button)findViewById(R.id.buttonNextFrame);
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
        //set our listeners to m_score the new frame's throws.
        m_firstThrowScoreListener.setThrowToScore(frame.firstThrow);
        m_secondThrowScoreListener.setThrowToScore(frame.secondThrow);

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

        //if the first throw hasn't been done yet
        if(frame.firstThrow.throwType == ThrowType.NotThrown)
        {
            //set m_firstThrow & m_secondThrow texts to nothing
            m_firstThrow.setText("",TextView.BufferType.EDITABLE);

            //set the second throw text to be disabled due to a first throw not happening yet.
            m_secondThrow.setText("",TextView.BufferType.EDITABLE);
            m_secondThrow.setEnabled(false);
        }
        else //the first throw has been completed.
        {
            //set the text of the first throw editText to the first throw's m_score
            m_firstThrow.setText(((Integer)(frame.firstThrow.m_score)).toString(), TextView.BufferType.EDITABLE);

            //make sure that the secondThrow is enabled since we have a first throw.
            m_secondThrow.setEnabled(true);

            //if our second throw hasn't been thrown, display no text for it
            if (frame.secondThrow.throwType != ThrowType.NotThrown)
            {
                m_secondThrow.setText("", TextView.BufferType.EDITABLE);
            }
            else
            {
                m_secondThrow.setText(((Integer)(frame.secondThrow.m_score)).toString(), TextView.BufferType.EDITABLE);
            }
        }
    }
}