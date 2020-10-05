package edu.rit.cs.project1;

//Id,ProductId,UserId,ProfileName,HelpfulnessNumerator,HelpfulnessDenominator,Score,Time,Summary,Text
public class AmazonFineFoodReview {

    private int _Id;
    private String _ProductId;
    private String _UserID;
    private String _ProfileName;
    private int _HelpfulnessNumerator;
    private int _HelpfulnessDenominator;
    private int _Score;
    private long _Time;
    private String _Summary;
    private String _Text;

    public AmazonFineFoodReview(String reviewLine) {
        String otherThanQuote = " [^\"] ";
        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
        String regex = String.format("(?x) "+ // enable comments, ignore white spaces
                        ",                         "+ // match a comma
                        "(?=                       "+ // start positive look ahead
                        "  (?:                     "+ //   start non-capturing group 1
                        "    %s*                   "+ //     match 'otherThanQuote' zero or more times
                        "    %s                    "+ //     match 'quotedString'
                        "  )*                      "+ //   end group 1 and repeat it zero or more times
                        "  %s*                     "+ //   match 'otherThanQuote'
                        "  $                       "+ // match the end of the string
                        ")                         ", // stop positive look ahead
                otherThanQuote, quotedString, otherThanQuote);

        String [] data = reviewLine.split(regex, -1);

        try {
            this._Id = Integer.valueOf(data[0]);
            this._ProductId = data[1];
            this._UserID = data[2];
            this._ProfileName = data[3];
            this._HelpfulnessNumerator = Integer.valueOf(data[4]);
            this._HelpfulnessDenominator = Integer.valueOf(data[5]);
            this._Score = Integer.valueOf(data[6]);
            this._Time = Long.valueOf(data[7]);
            this._Summary = data[8];
            this._Text = data[9];
        }catch(Exception e){
            System.err.println(e.toString());
        }
    }


    public int get_Id() {
        return _Id;
    }

    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public String get_ProductId() {
        return _ProductId;
    }

    public void set_ProductId(String _ProductId) {
        this._ProductId = _ProductId;
    }

    public String get_UserID() {
        return _UserID;
    }

    public void set_UserID(String _UserID) {
        this._UserID = _UserID;
    }

    public String get_ProfileName() {
        return _ProfileName;
    }

    public void set_ProfileName(String _ProfileName) {
        this._ProfileName = _ProfileName;
    }

    public int get_HelpfulnessNumerator() {
        return _HelpfulnessNumerator;
    }

    public void set_HelpfulnessNumerator(int _HelpfulnessNumerator) {
        this._HelpfulnessNumerator = _HelpfulnessNumerator;
    }

    public int get_Score() {
        return _Score;
    }

    public void set_Score(int _Score) {
        this._Score = _Score;
    }

    public long get_Time() {
        return _Time;
    }

    public void set_Time(long _Time) {
        this._Time = _Time;
    }

    public String get_Summary() {
        return _Summary;
    }

    public void set_Summary(String _Summary) {
        this._Summary = _Summary;
    }

    public String get_Text() {
        return _Text;
    }

    public void set_Text(String _Text) {
        this._Text = _Text;
    }
}
