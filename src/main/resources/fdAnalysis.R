# Find pairs of study/assay file names in investigation file
find.saFiles <- function(inv) {
  
  print("[debug] find.saFiles")
  
  files <- data.frame(studyName=character(0), assayName=character(0), stringsAsFactors=FALSE)
  
  inv.studies <- as.matrix(subset(inv, V1=="Study File Name")) 
  inv.assays <- as.matrix(subset(inv, V1=="Study Assay File Name"))
  
  for (i in 1:dim(inv.studies)[1]) {
    sName <- inv.studies[i,2]
    for (j in 2:dim(inv.assays)[2]) {
      aName <- inv.assays[i,j]
      if (is.null(aName) || aName =="") {
        break
      }
      else {
        pair <- c(sName, aName)
        files <- rbind(files, pair)
      }
    }
  }
  files  
}

# Read standard investigation file in given folder
read.iFile <- function(folder=".", file="i_Investigation.txt") {
  
  print("[debug] read.iFile")
  
  iFile <- file
  inv = tryCatch({
    fname = paste(folder, file, sep="/")
    read.delim(fname, header=F)
  },    
  error = function(e)       
    print(e),
  warning = function(w) {
    iFile <- find.iFile(folder)
    inv=read.iFile(folder, iFile)
  },
  finally = function() {
    on.exit(close(fname))
  }
  )
  
  list(i=inv, iFile=iFile)
}

# Find non-standard investigation file in given folder
find.iFile <- function(dir) {
  
  print("[debug] find.iFile")
  
  nums <- grep("^i_.*", list.files(path=dir))
  if (length(nums) == 0) {
    stop(paste("ERROR: No investigation files were found in folder", dir))
  }
  if (length(nums) > 1) {
    stop(paste("ERROR: More than one (", length(nums), ") investigation files were found in folder", dir))
  }
  inv <- list.files(path=dir)[nums]
  inv
}

# Find isa files - first investigation, then study/assay pairs
get.isaFiles <- function(dir=".") {
  
  print("[debug] get.isaFiles")
  
  tmp <- read.iFile(dir)
  inv <- tmp$i
  iFile <- tmp$iFile
  saFiles <- find.saFiles(inv)
  isaFiles <- cbind(iFile, saFiles)
  isaFiles
}

# Load metadata for study/assay pair
load.saFiles <- function(sName, aName) {
  
  print("[debug] load.saFiles")
  
  print(paste(sName, aName))
  s <- read.table(sName, header=T, sep='\t')
  s.names <- as.vector(names(read.table(sName, header=T, sep="\t", check.names=F)))
  names(s.names) <- names(s)
  print("[debug]     read.table study")
  
  a <- read.table(aName, header=T, sep='\t', fill=T)
  a.names <- as.vector(names(read.table(aName, header=T, sep='\t', fill=T, check.names=F)))
  names(a.names) <- names(a)
  print("[debug]     read.table assay")
  
  s <<- s
  a <<- a
  sa.names <- c(s.names, a.names)
  
  dupNames <- subset(names(s), match(names(s), names(a)) > 0)
  print(paste("Common columns: ", toString(dupNames)))
  dupNames.nontrivial <- grep("(REF)|(Accession.Number)", dupNames, invert=T)
  dupNames <- dupNames[dupNames.nontrivial]
  warning("Parameter all=FALSE, not paired rows will be removed")
  sa <- merge(s, a, by=dupNames, all=F)
  print(paste("Merged by", paste(dupNames, collapse=", ")))
  
  if (dim(sa)[1] == 0) {
    stop(paste("ERROR: Matching study file", sName, "and assay file", aName, "by columns", paste(dupNames, collapse=", "), 
               "resulted in an empty set. Check for conflicting values."))
  }
  
  list(sa, sa.names)
}

# Load data for study/assay pair
load.dFile <- function(dFile) {
  
  print("[debug] load.dFile")
  print(paste("Loading", dFile))
  
  f <- paste(getwd(),dFile, sep="/")
  
  d <- tryCatch({
    if (length(grep("xls", dFile)) > 0) {
      if (exists("PERL"))
        load.xls(f)
      else
        load.txt(f)
    }
    else {
      print(paste("Loading", dFile, "with read.table"))
      load.txt(f)
      #read.table(f, header=T, sep="\t")
    }
  },    
  error = function(e) {
    write(paste("ERROR: Loading data from file", dFile, "failed. The following error occured: ", e), stderr())
  },
  warning = function(w) {
    write(paste("WARNING: Loading data from file", dFile, "produced a following warning: ", e), stderr())
  },
  finally = function() {
    on.exit(close(dataName))
  }
  )
  d
}

## NEEDS IMPROVMENT: get rid of perl
# Load data from xls file 
load.xls <- function(file) {
  
  print("[debug] load.xls")
  
  if(!require("gdata")) {install.packages("gdata", repos='http://cran.us.r-project.org')}
  library(gdata)
  d <- read.xls(file, perl=PERL)
  d2 <- read.xls(file, perl=PERL, check.names=F)
  d.names <- as.vector(names(d2))
  names(d.names) <- names(d)
  list(d, d.names)
}

# Load data from txt file
load.txt <- function(dFile) {
  
  print("[debug] load.txt")
  
  print(paste("Loading", dFile, "with read.table"))
  d <- read.table(dFile, header=T, sep="\t")
  d2 <- read.table(dFile, header=T, sep="\t", check.names=F)    
  d.names <- as.vector(names(d2))
  names(d.names) <- names(d)
  list(d, d.names)
}

# Find name of data file to use for study/assay pair
find.dFile <- function (sa) {
  
  print("[debug] find.dFile")
  are.files <- grep("(Raw)|(Derived)|(Processed).Data.File", names(sa), value=T)
  print(paste("Data files: ", toString(are.files)))
  

  have.all <- function(x) !any(is.na(x))
  are.full <- sapply(sa[are.files], have.all)
  
  if (length(are.full) < 1)
    stop("ERROR: Among the columns referring to data there are no columns free of missing values")
  
  have.same <- function(x) length(unique(x))==1
  are.same <- sapply(sa[are.files][are.full], have.same)
  
  nSame = length(sa[are.files][are.full][are.same])
  if ( nSame < 1)
    stop("ERROR: Among the columns referring to data there are no full columns with all same values")
  if ( nSame > 1)
    warning("WARNING: Among the columns referring to data there are more than one full columns with all same values.")
  
  print(paste("Full equal data names in: ", toString(are.files[are.full][are.same])))
  

datacol <- are.files[grep(pat="data_process", sa[are.files][are.full][are.same])]
print(paste("Using data from file: ", datacol))
datafile <- sa[1, datacol]  
datafile
}

# Merge all tables for the experiment into sad table
get.sad <- function(sa, d) {
  
  print("[debug] get.sad")
  
  dupNames <- subset(names(sa), match(names(sa), names(d)) > 0)
  print(paste("Common columns: ", toString(dupNames)))
  dupNames.nontrivial <- grep("(REF)|(Accession.Number)", dupNames, invert=T)
  dupNames <- dupNames[dupNames.nontrivial]
  sad <- merge(sa, d, by=dupNames, all=T)
  print(paste("Merged by: ", toString(dupNames)))
  
  if (dim(sad)[1] == 0) {
    stop(paste("ERROR: Matching study file", sName, "and assay file", aName, "with data file", d, "by columns", paste(dupNames, collapse=", "), 
               "resulted in an empty set. Check for conflicting values."))
  }
  
  sad
}

do.fda <- function(data, timepoints, nbases=10, norder=3) {
  
  print("[debug] do.fda")
  
  require("fda")
  
  bspl <- create.bspline.basis(rangeval=c(min(timepoints),max(timepoints)), nbasis=nbases, norder=norder) 
  fd <- Data2fd(y=data, argvals= timepoints, basisobj=bspl)
  
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

# Save results to files
save.fda.results <- function (sFile, aFile, result) {
  
  print("[debug] save.sufficient.results")
  
  sFile2 <- substr(sFile, start=0, stop=regexpr("[.]",sFile)-1)
  aFile2 <- substr(aFile, start=0, stop=regexpr("[.]",aFile)-1)
  
  fname <- paste(paste("data_fda", sFile2, aFile2, sep="_"),".txt",sep="")
  write.table(result, file=paste("results/", fname, sep=""), sep="\t", na="", row.names=F, quote=F)
  print(paste("FDA analysis results saved to file: ", fname))
  
  fname
} 

#Udpade inevstigation by adding info about a protocol for sufficient
update.fda.investigation <- function(ipath) {
  
  print("[debug] update.fda.investigation")
  
  
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
  print("Investigation file updated.")
}

# Update assay file to include sufficient statistics column
update.fda.aFile <- function(aFile, fname) {
  
  print("[debug] update.aFile")
  
  print("Updating assay file..")
  a <- read.table(aFile, header=T, check.names=F, sep="\t")
  a <- cbind(a, "Protocol REF"="functional time data analysis", "Derived Data File"=fname)
  
  if (length(grep(aFile, pat="results/")) == 0){
    aFile <- paste("results/", aFile, sep="")
  }
  write.table(a, na="", row.names=F, sep="\t", quote=F, file=aFile)  
  print(paste("Assay file", aFile, "updated to include sufficient statistics column"))
  
}

# Zip files
zip.sufficient.files <- function() {
  
  print("[debug] zip.sufficient.files")
  
  files.new <- list.files("results", full.names = T)
  files.old <- list.files(include.dirs = FALSE, pattern = "[^.zip]$")
  files.unchanged <- setdiff(files.old, list.files("results"))
  files <- c(files.unchanged, files.new)
  zip(zipfile="original.zip", files=files.old, flags = "-j")
  zip(zipfile="results.zip", files=files, flags = "-j")
}

run.analysis <- function(sad, sad.names, traits) {
  
  print("[debug] run.analysis")
  
  timecol.id <- grep(pattern="time", names(sad))                                 #ids of columns with timestamp to be analysed (hopefull just one)
  if (length(timecol.id)==0) {
    stop("ERROR: No time attribute for functional time data analysis found.")
  }
  timecol <- names(sad)[timecol.id]
  varcol.id <- grep(pattern="(Factor|Characteristics)", names(sad))                #ids of columns with variables
  varcol.id2 <- grep(pattern="(i|I)(d|D)", names(sad), invert = T)                #ids of columns with variables
  varcol.id <- intersect(varcol.id, varcol.id2)
  varcol.id <- setdiff(varcol.id, timecol.id)
  varcol <- names(sad)[varcol.id]
  fun.is.multivalued <- function(x) { length(unlist(unique(sad[x]))) > 1}
  are.multivalued <- unlist(lapply(varcol, FUN=fun.is.multivalued))
  varcol <- varcol[are.multivalued]
  
  meta <- sad[c("Sample.Name", "Assay.Name", varcol, timecol, traits)]                            #columns with variables + ids
  meta$timepoints <- strptime(meta[[timecol]], "%m/%d/%Y")                         #add formatted dates
  
  
  require(reshape2)
  
  out.traits <- data.frame()
  out.scores <- data.frame()
  
  formula <- paste(paste(varcol, collapse="+"), "~", "timepoints") #TODO decide what arguments should proceed ~ 
  fun.non.empty <- function(x) {!any(is.na(x)) & !any(is.nan(x)) }
  
  #run fda analysis for each trait
  for (i in traits) {
    
    #TODO co zrobić z podwojnymi obserwacjami? tymczasowo średnia
    timeseries <- acast(meta, formula=formula, value.var = i, fun.aggregate = mean)
    timeseries2 <- dcast(meta, formula=formula, value.var = i, fun.aggregate = mean)
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
    
    scores <- as.data.frame(fpca$scores)
    names(scores) <- paste("FPCA score", 1:numberOfComponents)
    scores <- cbind(timeseries2[,1:2], scores)
    
    sm <- melt(scores, id=varcol)
    
    scores.melt <- cbind(levels(sm$variable)[sm$variable], sm[varcol], timecol="", sm$value, 0)
    colnames(scores.melt) <- c("Parameter", varcol, timecol, paste("Estimate[", i, "]", sep=""), paste("Standard Error[", i, "]", sep=""))
    if (length(out.scores)==0) {
      out.scores <- rbind(out.scores, scores.melt)
    } else {
      out.scores <- merge(out.scores, scores.melt, by=c("Parameter", varcol, timecol), all=T)
    }
    
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
    colnames(out.trait) <- c("Parameter", timecol, paste("Estimate[",i,"]", sep=""), paste("Standard Error[", i, "]", sep=""))
    
    if (length(out.traits) == 0){
      out.traits <- out.trait
    } else {
      out.traits <- cbind(out.traits, out.trait[,c(-1,-2)])
    }    
  }
  
  out <- data.frame(Parameter=out.traits[,1])
  out[,varcol] <- ""
  out <- cbind(out, out.traits[,-1])
  out <- rbind(out, out.scores)
  
  ns <- names(out)
  for (i in 1:length(ns)) {
    rep <- sad.names[ns[i]]
    if (!is.na(rep)) {
      names(out)[i] <-rep
    }
  }
  
  out
}




# Run processing: find, read, get stats, save
run <- function() {
  
  print("[debug] run")
  #r <- regexpr(pattern="/[^/]*$", getwd())
  #dir <- substr(getwd(), r[1]+1, r[1]+attr(r, "match.length"))
  
  saPairs <- get.isaFiles()#"isatab - Kopia")
  
  for (i in 1:dim(saPairs)[1]) {
    
    sFile <- saPairs[i,2]
    aFile <- saPairs[i,3]  
    tmp <- load.saFiles(sFile, aFile)
    sa <- tmp[[1]]
    sa.names <- tmp[[2]]
    
    dFile <- find.dFile(sa)
    saPairs[i,4] <- dFile
    dat <- load.dFile(dFile)
    d <- dat[[1]]
    d.names <- dat[[2]]
    
    sad <- get.sad(sa, d)
    sad.names <- c(sa.names, d.names)
    
    traits <- names(d)[-1]
    
    result <- run.analysis(sad, sad.names, traits)
    
    # update isa-tab file to include sufficient data file
    {
      results.exists <- length(grep(dir(), pat="^results$")) != 0
      if (!results.exists) {
        dir.create("results")
      }
      
      fname <- save.fda.results(sFile, aFile, result)
      saPairs[i,5] <- fname
      iFile <- saPairs[i,1]
      if (results.exists) {
        aFile <- paste("results/", aFile, sep="")
        iFile <- paste("results/", iFile, sep="")
      }
      update.fda.aFile(aFile, fname)   
      update.fda.investigation(iFile)
      zip.sufficient.files()
    }

  }
} 




args <- commandArgs(TRUE)
if (length(args) > 0) {
  setwd(args[1])
}

options(stringsAsFactors=FALSE)
#setwd("C:/Users/hcwi/Dropbox/IGR/phenalyse/phen-stats - new/isatab - Kopia")
#path <- "C:/Users/hnk/Dropbox/IGR/phenalyse/FDA/data/ExampleKeyGene"
#setwd(path)

run()