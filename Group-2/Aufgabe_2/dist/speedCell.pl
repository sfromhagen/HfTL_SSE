#!perl
use utf8;
use IO::Socket::INET;
use DateTime;

# initialize host and port
my $host = $ARGV[0];
my $port = $ARGV[1];

# speed.pl localhost 4242
# speed.pl host port

my $readingCommando = "gatttool -t random -b E0:00:3F:92:2F:E8 --char-write-req --handle=0x002d --value=0100 --listen";
#my $readingCommando = "ping 192.168.2.1";
#my $readingCommando = "perl /opt/test.pl"; print "Starting sensor\n";

our $socket = IO::Socket::INET->new(PeerAddr => $host,
                                PeerPort => $port,
                                Proto => "tcp",
                                Type => SOCK_STREAM)
    or die "Couldn't connect to $host:$port : $!\n";

# execute commando and pipe stdout to our $in filehandle for further processing
open $in, $readingCommando." |";

while (my $line = <$in>) {

    # comment if printing of the raw line is undesired
    print "Read raw line: $line";

   # $line =~ /(([0-9a-f]{2}(\s?)){1,12})/i;
    $line =~ /.*?\: (([0-9a-f]{2}(\s?)){1,12})/i;
    $hex = $1;

    # trim linebreaks and remove all whitespaces
    chomp ($hex);
    $hex =~ s/\s//g;
    print "Extracted hex: ",$hex,"\n";

    ($flags, $speed, $cadence, $rest ) = unpack("A2A4A2A*", $hex);

    $flags = hex($flags);
    $speed = hex(reverse($speed));
    $speed = $speed/256.0;
    $cadence = hex(reverse($cadence));

    if ($flags & 0b00000001){
        print "Stride Length present!\n";
        ($stride, $rest) = unpack("A4A*", $rest);
        $stride = hex(reverse($stride));
        $stride = $stride/100;
    }
    if ($flags & 0b00000010){
        print "Total Distance present!\n";
        ($distance, $rest) = unpack("A8A*", $rest);
        $distance = hex(reverse($distance));
        $distance = $distance/10;

    }
    if ($flags & 0b00000100){
        print "Status: Running\n";
    }else{
        print "Status: Walking\n";
    }

    print "Speed[m/s]: $speed\n";
    print "Cadence[rpm]: $cadence\n";
    print "Stride[m]: (not supported) $stride\n";
    print "Distance[m]: $distance\n\n";


    # Transfer the data to IGSAM (implementation by Group2 - SMM)

    my $dt = DateTime->now; # Stores current date and time as datetime object
    $dt->add(hours => 2);

    my $date = $dt->ymd; # Retrieves date as a string in 'yyyy-mm-dd' format
    my $time = $dt->hms; # Retrieves time as a string in 'hh:mm:ss' format

    my $line = join(";",$date."T".$time.".123+02:00","SpeedSensor","measuredSpeed",$speed);
    print $socket "$line\n";
    print "$line\n";

}
close($in);

	