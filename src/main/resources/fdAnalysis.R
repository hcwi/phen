do.fda <- function(data, timepoints, nbases=10, norder=3) {
  
  print("[debug] do.fda")
  
  lib <- "fda"
  if(length(grep(pattern = lib, installed.packages())) == 0){
    print(paste("Library", lib, "missing. Installing"))
    install.packages(lib)
  }
  require(lib, character.only = TRUE)
  
  bspl <- create.bspline.basis(rangeval=c(min(timepoints),max(timepoints)), nbasis=nbases, norder=norder) 
  fd <- Data2fd(y=data, argvals= timepoints, basisobj=bspl)
  
  #par(mfrow=c(3,1))
  #plot(data[,1],type="l",col="red", ylim=c(min(data), max(data)))           #plot original data
  #for (i in c(2:dim(data)[2]))
  #  lines(data[,i],col=i)
  #grid()
  #plot.fd(fd, xlab="time", xlim=c(min(timepoints),max(timepoints)))         #plot fd mapping
  #grid()
  #plot.fd(mean(fd), xlab="time", xlim=c(min(timepoints),max(timepoints)))   #plot mean fd mapping
  #grid()
  #dev.copy2pdf(file="profiles and mean.pdf")
  #dev.off()
  #########
  
  ### check suspicious values ###
  # id <- c(1:200)[data[24,]>40000] 
  # par(mfrow=c(2,1))
  # plot.fd(fd_new[id], xlab="time", xlim=c(min(timepoints),max(timepoints)))
  # grid()
  # plot(data[,id[1]],type="l", ylim=c(min(data), max(data)))
  # for (i in id[-1])
  #    lines(data[,i],col=i)
  # grid()
  ########
  
  fd
}

print.fpca <- function(fpca) {
  
  print("[debug] print.fpca")
  
  dev.new() 
  par(mfrow=c(3,2))
  plot.pca.fd(fpca)
  dev.copy2pdf(file="FPCA_component_variability.pdf")
  dev.off()
  
  dev.new()
  plot(fpca$harmonics, xlab="time (min.)")
  title('PCA eigenfunctions')
  dev.copy2pdf(file="FPCA_eigenfunctions.pdf")
  dev.off()
  
  # matrix of scores
  write.table(x=fpca$scores, file="PCAscores.txt", quote=FALSE, sep = "\t", row.names=FALSE, col.names = TRUE )
  # % variance explained by components
  write.table(x=fpca$varprop, file="PCA_varprop.txt", quote=FALSE, sep = "\t", row.names=FALSE, col.names = TRUE )
}

update.investigation <- function(ipath) {
  
  print("[debug] update.investigation")
  
  inv <- read.csv(ipath, sep = "\t", head=F)
  inv <- as.matrix(inv)
  prot <- grep(pat="Study Protocol Name", inv)
  ncols <- dim(inv)[2]
  prot.count <- max((1:ncols)[inv[prot,] != "" & !is.na(inv[prot,])]) 
  if (prot.count == ncols) {
    inv <- cbind(inv, NA)
  } 
  inv[prot, prot.count+1] <- "functional time data analysis"
  inv[grep(pat="Study Protocol Type$", inv), prot.count + 1] <- "data analysis"
  desc <- "Time data analysis based on functional data approach. Individual plant time profiles for traits are approximated by bsplines of a fixed order. From them, a number of principal components are estimated, and serve for evaluation of the trait values at given timepoints"
  inv[grep(pat="Study Protocol Description", inv), prot.count + 1] <- desc
  
  if (length(grep(ipath, pat="results/")) == 0){
    ipath <- paste("results/", ipath, sep="")
  }
  write.table(x=inv, ipath, na="", col.names=F, sep="\t", quote = F, row.names=F)
}

# Zip files
zip.fda.files <- function() {
  
  print("[debug] zip.fda.files")
  
  files.new <- list.files("results", full.names = T)
  files.old <- list.files(include.dirs = FALSE, pattern = "[^.zip]$")
  files.unchanged <- setdiff(files.old, list.files("results"))
  files <- c(files.unchanged, files.new)
  zip(zipfile="original.zip", files=files.old, flags = "-j")
  zip(zipfile="results.zip", files=files, flags = "-j")
}


run.analysis <- function(ipath, apath, dpath) {
  
  print("[debug] run.analysis")
  
  ### load and prepare data ###
  assay.asis <- read.table(apath, header=TRUE, sep="\t", check.names = F)
  assay <- read.table(apath, header=TRUE, sep="\t")
  timecol.id <- grep(pattern="time", names(assay))                                 #ids of columns with timestamp to be analysed (hopefull just one)
  if (length(timecol.id)==0) {
    stop("ERROR: No time attribute for functional time data analysis found.")
  }
  timecol <- names(assay)[timecol.id]
  varcol.id <- grep(pattern="Factor|Characteristics", names(assay))                #ids of columns with variables
  varcol <- names(assay)[varcol.id]
  meta <- assay[c("Sample.Name", "Assay.Name", varcol)]                            #columns with variables + ids
  meta$timepoints <- strptime(meta[[timecol]], "%m/%d/%Y")                         #add formatted dates
  
  procdata <- read.table(dpath, header=TRUE, sep="\t")                             #load data file
  measurements <- merge(meta, procdata, by="Assay.Name")
  
  {
    #names(measurements)
    #xtabs(~Sample.Name, data=measurements)
    #xtabs(~Factor.Value.Replication., data=measurements)
    #freq <- xtabs(~Factor.Value.Snapshot.time.stamp., data=measurements)
    #plot(freq)
  } #testing
    
  formula <- paste("Sample.Name+Factor.Value.Replication.~", "timepoints") #TODO decide what arguments should proceed ~ 
  fun.non.empty <- function(x) {!any(is.na(x)) & !any(is.nan(x)) }
  traits <- names(procdata)[-1]
  
  require(reshape2)
  
  #run fda analysis for each trait
  out.traits <- data.frame()
  for (i in traits) {
    #TODO co zrobić z podwojnymi obserwacjami? tymczasowo średnia
    timeseries <- acast(measurements, formula=formula, value.var = i, fun.aggregate = mean)
    full.cols <- apply(timeseries, FUN=fun.non.empty, MARGIN = 2)
    full.timeseries <- timeseries[,full.cols]
    data <- t(full.timeseries)
    timepoints <- as.numeric(as.POSIXlt(row.names(data)))
    
    # FDA. Smoothing
    # nBases - number of basis functions for approximation
    # norder - the order of b-splines, which is one higher than their degree. The default of 4 gives cubic splines
    fd <- do.fda(data, timepoints, 10)
    
    # FPCA
    # numberOfComponents - number of functional principal components to estimate
    numberOfComponents = 6
    fpca <- pca.fd(fdobj = fd, nharm = numberOfComponents, harmfdPar=fdPar(fd), centerfns = TRUE)
    
    #Vector-valued matrix with the components
    components <- matrix(nrow = numberOfComponents , ncol = length(timepoints))
    for(j in 1:numberOfComponents){
      components[j,] = eval.fd(timepoints,fpca$harmonics[j])
    }
    # write.table(x=t(components), file=paste("components",i,".txt"), quote=FALSE,sep = "\t", row.names=FALSE, col.names = FALSE )
    # print.fpca(fpca)  
    out.trait <- data.frame()
    for(j in 1:numberOfComponents){
      out.j <- cbind(paste("Eigenfunction", j, "value"), row.names(data), components[j,], NA)
      if (j==1) { out.trait <- out.j
      } else { out.trait <- rbind(out.trait, out.j) }
    }
    colnames(out.trait) <- c("Parameter", names(assay.asis)[timecol.id], paste("Estimate[",i,"]", sep=""), paste("Standard Error[", i, "]", sep=""))
    
    if (length(out.traits) == 0){
      out.traits <- out.trait
    } else {
      out.traits <- cbind(out.traits, out.trait[,c(-1,-2)])
    }    
  }
  
  out <- data.frame(Parameter=out.traits[,1])
  ids <- c(names(assay.asis)[setdiff(varcol.id, timecol.id)])
  out[,ids] <- ""
  out <- cbind(out, out.traits[,-1])
  
  #save processed data file
  fname <- save.fda.results(ipath, apath, out)
  zip.fda.files()
  
  fname
}


save.fda.results <- function(ipath, apath, out) {
  
  print("[debug] save.fda.results")
  
  results.exists <- length(grep(dir(), pat="^results$")) != 0
  if (!results.exists) {
    dir.create("results")
  }
  
  print("[debug] run.analysis.save.results")
  fname <- paste("data_fda_", sep="", apath)
  write.table(out, file=paste("results/", fname, sep=""), quote=F, sep="\t", row.names=F, na="")
  
  #update assay file
  print("[debug] run.analysis.update.assay")
  if (results.exists) {
    assay.asis <- read.table(paste("results/",apath, sep=""), header=TRUE, sep="\t", check.names = F)
  } else {
    assay.asis <- read.table(apath, header=TRUE, sep="\t", check.names = F)
  }
  assay.asis.out <- cbind(assay.asis, "Protocol REF"="functional time data analysis", "Derived Data File"=fname)
  write.table(assay.asis.out, file=paste("results/", apath, sep=""), quote=F, sep="\t", na = "", row.names=F)
  
  #update investigation file
  if (results.exists) {
    ipath <- paste("results/", ipath, sep="")
  }
  update.investigation(ipath)
  
  fname
}


run <- function() {
  
  print("[debug] run")
  
  ipath <- grep(dir(), pat="^i_.*.txt", val=T)
  if (length(ipath) !=1) {
    stop("ERROR: No investigation file found.")
  }
  
  apath <- grep(dir(), pat="^a_.*.txt", val=T)
  if (length(apath) == 0) {
    stop("ERROR: No assay file found.")
  }
  
  dname <- grep(dir(), pat="^data_process.*.txt", val=T)
  if (length(dname) == 0) {
    stop("ERROR: No data file found.")
  }
  
  for (a in apath) {
    assay <- read.table(a, head=T, sep="\t")
    for (d in dname) {
      #check if dfile is mentioned in assay file
      for (i in 1:dim(assay)[2]) {
        if (length(grep(assay[,i], pat=d)) > 0) {
          print(paste("[debug] Run for", ipath, apath, dname))
          run.analysis(ipath, apath, dname)
          break;
        }
      }
    }
  }
  
  print("Finished")
  
}

print("Tutaj")
args <- commandArgs(TRUE)
if (length(args) > 0) {
  setwd(args[1])
}

#options(stringsAsFactors=FALSE)
#setwd("C:/Users/hcwi/Dropbox/IGR/phenalyse/phen-stats - new/isatab - Kopia")
#path <- "C:/Users/hnk/Dropbox/IGR/phenalyse/FDA/data/ExampleKeyGene"
#setwd(path)

run()


#m2 <- cbind( c(1,1,1,1,2,2,2,2,3,3,3,3), c(1,1,2,2,1,1,2,2,1,1,2,2), c(1,1,2,2,3,3,4,4,5,5,6,6))
#md <- data.frame(m2)
#md[,1] <- factor(m2[,1])
#md[,2] <- factor(m2[,2])
#md[,3] <- factor(m2[,3])
#model <- model.matrix(~.+0, md, contrasts.arg = lapply(md, contrasts, contrasts=FALSE))