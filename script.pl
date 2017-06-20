$content = $ARGV[0];
my @mail = split "\n", $content;

my $azure_m = 0;
my $google_m = 0;
my $imagga_m = 0;
my $watson_m = 0;

my $azure_err = 0;
my $google_err = 0;
my $imagga_err = 0;
my $watson_err = 0;

my @selected;
my @result;

my $google_str = "\$GOOGLE:";
my $azure_str  = "\$AZURE:";
my $watson_str = "\$WATSON:";
my $immaga_str = "\$IMAGGA:";

foreach $line (@mail) {
    if (index($line, "\$SELECTED:" ) != -1) {
        @selected = split ",", $line;
        @selected[0] =~ s/\$SELECTED://g;
    } elsif (index($line, $google_str ) != -1) {
        @result = split ",", $line;
        @result[0] =~ s/\$GOOGLE://g;
        ($google_m, $google_err) = check_and_augment(@result);
    } elsif (index($line, $azure_str ) != -1) {
        @result = split ",", $line;
        @result[0] =~ s/\$AZURE://g;
        ($azure_m, $azure_err) = check_and_augment(@result);
    }  elsif (index($line, $watson_str ) != -1) {
        @result = split ",", $line;
        @result[0] =~ s/\$WATSON://g;
        ($watson_m, $watson_err) = check_and_augment(@result);
    }  elsif (index($line, $immaga_str ) != -1) {
        @result = split ",", $line;
        @result[0] =~ s/\$IMAGGA://g;
        ($imagga_m, $imagga_err) = check_and_augment(@result);
    }
}

print $azure_m;
print ",";
print $azure_err;
print "!";
print $google_m;
print ",";
print $google_err;
print "!";
print $imagga_m;
print ",";
print $imagga_err;
print "!";
print $watson_m;
print ",";
print $watson_err;
print "\n";

sub check_and_augment {
    my @list = @_;
    my $m = 0;
    my $f = 0;
    my $temp;
    for $res (@list) {
        if ($res =~ /^[a-zA-Z]/) {
            $temp = 0;
            for $sel (@selected) {
                if ($res eq $sel) {
                    $temp = $temp + 1;
                }
            }
            $size = @list;
            if ($temp == 0) {
                $f = $f +1;
            } else {
                $m = $m + 1;
            }
        }
    }
    return ($m, $f);
}
