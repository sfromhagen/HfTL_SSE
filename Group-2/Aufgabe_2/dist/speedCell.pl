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

if ($host ne "offline"){
	our $socket = IO::Socket::INET->new(	PeerAddr => $host,
                                		PeerPort => $port,
                                		Proto => "tcp",
                                		Type => SOCK_STREAM)
    	or die "Couldn't connect to $host:$port : $!\n";
}


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
    print "Extracted hex: ",$hex,"\n";
    $hex =~ s/\s//g;

    ($flags, $speed, $cadence, $rest ) = unpack("a2a4a2a*", $hex);

    # speed needs preprocessing
    $speed= join('',reverse ( $speed =~ m/../g ));

    $flags = hex($flags);
    $speed = hex($speed);
    $speed = $speed/256.0;
    $speed_in_kmh = $speed * 3.6;
    $cadence = hex($cadence);

    if ($flags & 0b00000001){
        print "Stride Length present!\n";
        ($stride, $rest) = unpack("a4a*", $rest);
	
	# preprocessing to fix the byte order
    	$stride = join('',reverse ( $stride =~ m/../g ));

        $stride = hex($stride);
        $stride = $stride/100;
    }
    if ($flags & 0b00000010){
        print "Total Distance present!\n";
        ($distance, $rest) = unpack("a8a*", $rest);

	# preprocessing to fix the byte order
        $distance = join('',reverse ( $distance =~ m/../g ));

        $distance = hex($distance);
        $distance = $distance/10;

    }
    if ($flags & 0b00000100){
        print "Status: Running\n";
    }else{
        print "Status: Walking\n";
    }

    print "Speed[m/s]: $speed\n";
    print "Speed[km/h]: $speed_in_kmh\n";
    print "Cadence[rpm]: $cadence\n";
    print "Stride[m]: (not supported) $stride\n";
    print "Distance[m]: $distance\n\n";


    # Transfer the data to IGSAM (implementation by Group2 - SMM)

    my $dt = DateTime->now; # Stores current date and time as datetime object
    $dt->add(hours => 2);

    my $date = $dt->ymd; # Retrieves date as a string in 'yyyy-mm-dd' format
    my $time = $dt->hms; # Retrieves time as a string in 'hh:mm:ss' format

	# Send speed to CdD
    my $line = join(";",$date."T".$time.".123+02:00","SpeedSensor","measuredSpeed",$speed);
    print "$line\n";
    
    # Send distance traveled to CdD
    my $dline = join(";",$date."T".$time.".123+02:00","SpeedSensor","measuredDistance",$distance);
    print "$dline\n";

    if ($host ne "offline"){
		print $socket "$line\n";
		print $socket "$dline\n";
    }


}
close($in);

	
