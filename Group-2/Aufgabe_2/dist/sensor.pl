
#!/usr/bin/perl

use File::DirList;
use File::ReadBackwards;
use AnyEvent::Filesys::Notify;
use strict;
use IO::Socket::INET;

#open socket to sever


# initialize host and port
my $host = $ARGV[0];
my $port = $ARGV[1];

# client.pl localhost 4242 192.168.178.20
# client.pl host port server


# initialize host and port
# my $host = shift || 'localhost';
# my $port = shift || 7890;
# my $server = [AGSS]  # Host IP running the server

# create the socket, connect to the port

our $socket = IO::Socket::INET->new(PeerAddr => $host,
                                PeerPort => $port,
                                Proto    => "tcp",
                                Type     => SOCK_STREAM)
    or die "Couldn't connect to $host:$port : $!\n";


print "Socket to server: ".$host." open\n";







my $dir = '/opt/fhem/log/';
print "Now watching: $dir";

my $cv = AnyEvent->condvar;

my $notifier = AnyEvent::Filesys::Notify->new(
    dirs     => [$dir],
	
	#Callback, every time file from dir is changed...
    cb       => sub {
		# all new data since last notify
        my @events = @_;

        for my $event (@events) {
				
				# if file is changed..
				next if $event->type ne 'modified';

				# extract path of changed element.
                my $item = $event->path;
				
				# lead file backwards
                my $last  = File::ReadBackwards->new($item)->readline;
				
				# last line is: 2016-04-24_13:19:07 Heizung_Bad measured-temp: 25.2

                $last =~ /^(\S+)\s(\S+)\s(\S+):\s(\d+\.\d)/;
                #$1 = timestamp
				#$2 = tendent ID = DeviceName, here: Heizung_Bad7
				#$3 = measurement type, here: measured-temp (Without ":")
				#$4 = value, here: 25.2
				
				
				if ($1){
				# if something is found 
                        print "Send: ".$last." to server\n";
                        
						#refactor timestamp
						
						
						# send data via socket to server
						my $line = join(";",$1,$2,$3,$4);
						print $socket "$line\n";				
						#while ($line = <SOCKET>) {
						#print "$line\n";
						#}
						
						
						
                }
        }
    },
);

$cv->recv;
close SOCKET or die "close: $!";
