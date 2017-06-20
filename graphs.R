#!/usr/bin/env Rscript

f = function(a, b, name, scale, path) {

    png(filename=sprintf("%s/%sGraph.png", path, name))
    barplot(
        c(a, b),
        main=sprintf("%s accuracy", name),
        col=c("darkgreen", "darkred"),
        ylim=c(0, scale),
        names.arg=c("Matches", "Errors"),
        ylab="Frequency"
    )
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

args = commandArgs(trailingOnly=TRUE)
if (length(args)==0) {
  stop("At least one argument must be supplied (path to save graphs).n", call.=FALSE)
} else {
    
    dati = read.csv('csv.csv', sep=",")
    g(dati, args[1])
}
