#!/usr/bin/env Rscript

f = function(a, b, name, scale, path) {

    png(filename=sprintf("%s/%sGraph.png", path, name))
    xx <- barplot(
        c(a, b),
        main=sprintf("%s accuracy", name),
        col=c("darkgreen", "darkred"),
        ylim=c(0, scale),
        names.arg=c("Matches", "Mismatch"),
	cex.lab=2,	
	cex.axis=2,
	cex.names=2,
	cex.main=2
    )
    text(x = xx, y = c(a,b), label = c(a,b), pos = 3, cex = 2)
    dev.off()
}

g = function (dati, path) {
    
    colors=c("darkgreen", "red")
    rows=nrow(dati)
    
    for (i in 1:rows) {
        name = dati[i, 1]
        
        f(dati[i,2], dati[i,3], name, (max(dati[,2:3])) + 100, path)
    }
    
    name = "Overall"
    f(sum(dati[,2]), sum(dati[,3]), name, sum(dati[,2:3]) + 100, path)
}

h = function(a, b, c, d, name, scale, path, filename) {     
    png(filename=sprintf("%s/%sGraph.png", path, filename))
    xx <- barplot(
        c(a, b, c, d),
        main=sprintf("%s", name),
        col=c("lightsteelblue3", "brown3", "chartreuse4", "darkgoldenrod1"),
        ylim=c(0, scale),
        names.arg=c("Azure", "Google", "Imagga", "Watson"),
	cex.axis=2,
	cex.names=2,
	cex.main=2
    )
    text(x = xx, y = c(a,b,c,d), label = c(a,b,c,d), pos = 3, cex = 2)
    dev.off()
}

i = function(dati, path) {
    rows=nrow(dati)
    
    name="At least one match"
    filename = "AtLeastOneMatch"
    h(dati[1,4], dati[2,4], dati[3,4], dati[4,4], name, 150, path, filename)
    
    name="Complete match"
    filename="CompleteMatch"
    h(dati[1,5], dati[2,5], dati[3,5], dati[4,5], name, 150, path, filename)
}

args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
    stop("At least one argument must be supplied (path to save graphs).n", call.=FALSE)
} else {
    dati = read.csv('csv.csv', sep=",")
    g(dati, args[1])
    i(dati, args[1])
}
