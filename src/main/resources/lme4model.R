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
  
  s <- s
  a <- a
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
                  write(paste("WARNING: Loading data from file", dFile, "produced a following warning: ", w), stderr())
                },
                finally = function() {
                  on.exit(close(dataName))
                }
  )
  d
}

# Load data from xls file
load.xls <- function(file) {

  print("[debug] load.xls")

  if(!require("gdata")) {
    stop("Library gdata cannot be leaded. Check if installed. Stopping execution")
   }
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

  cols <- are.files[are.full][are.same]
  nSame = length(sa[are.files][are.full][are.same])
  if ( nSame < 1) {
    stop("ERROR: Among the columns referring to data there are no full columns with all same values")
  } else if ( nSame > 1) {
    warning("WARNING: Among the columns referring to data there are more than one full columns with all same values.")
    print(paste("Full equal data names in: ", cols))
    datacol <- grep(pat="Derived", are.files, val=T)[1]
  } else {
    datacol <- cols[1]
  }

  print(paste("Using data from file: ", datacol))
  datafile <- sa[1, datacol]
  datafile <- gsub(" *$", '', x=datafile)
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


# Check if random effect exists - if not, add a random column
check.random <- function(sad, sad.names) {

  print("[debug] check.random")

  if (length(get.random(sad)) == 0 ) {
    sad <- cbind(sad, Factor.Value.Random.=sample(1:10, dim(sad)[1], rep=T))
    l <- length(sad.names) + 1
    sad.names[l] <- "Factor Value[Random]"
    names(sad.names)[l] <- "Factor.Value.Random."

    print("      Random column added")
  }
  list(sad=sad, names=sad.names)
}

# Change names from R-consumable to input-like strings
change.names <- function (means, names) {

  print("[debug] change.names")

  old.names <- colnames(means)
  old.names <- gsub("S[.]e[.]", "", old.names)
  names[old.names]

  chnames <- function(x) {
    if (!is.na(names[x])) {
      if(length(grep("Trait Value", names[x])) == 0) {
        names[x][1]
      }
      else {
        gsub("Trait Value", "Estimate", names[x][1])
      }
    }
    else {
      x
    }
  }
  new.names <- sapply(old.names, chnames)

  for (i in 2:length(new.names)) {
    if (new.names[i] == new.names[i-1]) {
      new.names[i] <- gsub("Estimate", "Standard Error", new.names[i])
    }
  }

  colnames(means) <- new.names
  means
}


# Calculate models and final statistics
get.models <- function(sad, sad.names, traits) {

  print("[debug] get.models")
  prepare.libs()

  #traits <- get.traits(sad)
  results <- prepare.results(sad, traits)

  success <- length(traits);
  models <- list()
  for (i in 1:length(traits)) {

    trait = traits[i]

    tmp <- tryCatch(
      get.model.for.trait(trait, sad, results, traits),
        error = function(e){
          #write(paste("ERROR: Evaluating of model for trait '", sad.names[trait], "' failed. The following error occured: ", e, sep=""), stderr())
          stop(#"ERROR: Evaluating of model for trait '", sad.names[trait], "' failed. The following error occured: ",
          e)
          return(NA)
        },
        warning = function(w) {
          write(paste("WARNING: Evaluating of model for trait '", sad.names[trait], "' produced the following warning: ", w, sep=""), stderr())
          return(tmp)
        },
        finally = function() {}
    )

    if (!is.na(tmp[1])) {
      results <- tmp$results
      info <- tmp$info
      models <- c(models,list(info))
      success <- success-1;
    }

  }

  list(means=results, models=models, success=success)
}


# Calculate model and statistics for a trait
get.model.for.trait <- function(trait, sad, results, traits) {

  print(paste("Models for a new trait:", trait))

  sadt <- sad[!is.na(sad[,trait]),]

  fixed <- get.fixed(sadt)
  timecol <- grep(pat="time", fixed, val=T)
  varcol <- setdiff(fixed, timecol)
  fixef <- paste(varcol, collapse="*")
  if (length(timecol) > 0) {
    fixef <- paste(fixef, sep="+", paste(timecol, collapse="+"))
    stop("ERROR Time dimension identified in the dataset is not suitable for model estimation.")
  }
  #fixef <- paste(fixed, collapse="*")
  random <- get.random(sadt)
  ranef <- paste("+(1|", random, ")", sep="", collapse="")

  factorizenames <- function(x) {
    if (is.numeric(sadt[,x]) &&
          #length(grep("Trait[.]Value", names(sadt)[x])) > 0
          any(traits==names(sadt)[x])
        )
      sadt[,x]
    else factor(sadt[,x])
  }
  sadtf <- lapply(seq_along(sadt), FUN=factorizenames)
  names(sadtf) <- names(sadt)

  form <- paste(trait,"~",fixef, ranef, sep="")
  print(paste("Formula:", form))

  model <- lmer(form, sadtf)

  # Set variances of random effects
  {
    for (j in 1:length(random)) {
      r <- random[j]
      v <- VarCorr(model)[[r]][1]

      is.variable <- !is.na(results[,r]=="*")
      is.variance <- results[,"Parameter"] == "Variance"
      results[is.variable & is.variance, trait] <- v
    }
  }

  # Set sigma
  {
    errvar <- attr(VarCorr(model), "sc")^2
    is.errvar <- results[,"Parameter"] == "Error variance"
    results[is.errvar, trait] <- errvar
  }

  # Set means for fixed effects
  {
    results <- fill.means.for.fixed(sad, results, model, fixed, trait)
  }

  info <- list(trait=trait, fixed=fixed, random=random, model=model)

  list(results=results, info=info)
}



# Prepare table for results (combinations of effects)
prepare.results <- function(sad, traits) {

  print("[debug] prepare.results")

  type <- "Parameter"
  form <- "Formula"
  #traits <- get.traits(sad)
  traits.se <- paste("S.e.", traits, sep="")
  fixed <- get.fixed(sad)
  random <- get.random(sad)

  cols <- c(type, form, fixed, random, c(rbind(traits, traits.se)))
  ncols <- length(cols)

  results <- matrix(nrow=1, ncol=ncols)
  colnames(results) <- cols

  # General mean
  {
    srow <- 1
    results[srow, form] <- ""
    results[srow, type] <- "Mean"
  }

  # Means for fixed effects
  {
    srow <- 2
    for (i in 1:length(fixed)) {
      com <- combn(fixed, i)
      ncom <- dim(com)[2]
      for (j in 1:ncom) {
        names <- com[,j]
        dat <- unique(sad[names])
        dat <- as.matrix(dat[order(dat[1]),])

        nrows <- dim(dat)[1]
        if (is.null(nrows)) nrows <- length(dat)
        results <- rbind(results, matrix(nrow=nrows, ncol=ncols))

        formula <- paste(names, collapse="*")

        to <- srow + nrows - 1
        results[srow:to, names] <- dat
        results[srow:to, form] <- formula
        results[srow:to, type] <- "Mean"
        results
        srow <- to + 1
      }
    }
  }

  # Variances of random effects
  {
    nrows <- length(random)
    results <- rbind(results, matrix(nrow=nrows, ncol=ncols))
    for (i in 1:nrows) {
      results[srow, random[i]] <- "*"
      results[srow, type] <- "Variance"
      srow <- srow + 1
    }
  }

  # Error variance
  {
    results <- rbind(results, matrix(nrow=1, ncol=ncols))
    results[srow, type] <- "Error variance"
    srow <- srow + 1
  }

  results
}

# Calculate and fill estimated means for fixed effects
fill.means.for.fixed <- function(sad, results, model, fixed, trait) {

  print("[debug] fill.means.for.fixed")

#         timecol <- grep(pat="time", fixed, val=T)
#         varcol <- setdiff(fixed, timecol)
#         tmp <- prepare.matrices(sad, varcol)
#         fix <- tmp$fix
#         xu <- tmp$xu
#         tmp2 <- prepare.matrices(sad, timecol)
#         fix2 <- tmp2$fix
#         xu2 <- tmp2$xu
#
#         fix2[,3:4] <- fix2[,3:4] + fix[dim(fix)[1],4]-1
#         fix3 <- rbind(fix, fix2[-1,])
#
#         xu3 <- cbind(xu, xu2[,-1])
#
#         fix <- tmp$fix


  tmp <- prepare.matrices(sad, fixed)
  fix <- tmp$fix
  xu <- tmp$xu

  x <- unique(getME(model, "X"))
  est <- x %*% fixef(model)
  est.cov <- x %*% vcov(model) %*% t(x)

  for (i in 1:dim(fix)[1]) {

    factor <- fix[i,]$mform
    from <- fix[i,]$from
    to <- fix[i,]$to

    xf <- xu[,from:to]
    m <- solve(t(xf) %*% xf) %*% t(xf)
    means <- m %*% est
    rownames(means) <- colnames(xu)[from:to]

    means.var <- diag(m %*% est.cov %*% t(m))

    a <- results[,"Formula"] == factor
    b <- results[,"Parameter"] == "Mean"
    results[a&b, trait] <- means
    results[a&b, paste("S.e.",trait, sep="")] <- sqrt(means.var)
  }

  results
}

# Prepare full model matrix and indices for it
prepare.matrices <- function(sad, fixed) {

  print("[debug] prepare.matrices")

  fix <- data.frame(mform="", pform="", from=1, to=1)

  x <- matrix(1, nrow=dim(sad)[1])
  colnames(x) <- "all"

  f=1
  for (i in 1:length(fixed)) {
    com <- combn(fixed, i)
    ncom <- dim(com)[2]
    for (j in 1:ncom) {
      names <- com[,j]
      mform <- paste(names, collapse="*")
      pform <- paste(names, collapse=":")

      formula <- paste("Assay.Name", sep="~", mform)
      print(paste("[debug]           formu: ", formula, sep=""))

      xtmp <- cast(sad, formula, length)
      xtmp <- xtmp[-1]
      x <- cbind(x, xtmp)

      from <- fix[f, "to"] + 1
      to <- from + dim(xtmp)[2] - 1
      f <- f + 1
      fix[f,] <- list(mform, pform, from, to)
    }
  }

  xu <- as.matrix(unique(x))

  list(fix=fix, xu=xu)
}

# Install and load missing libraries
prepare.libs <- function() {

  if(!require("lme4")) {
    stop("Library lme4 cannot be loaded. Check if it is installed. Stopping execution.")
  }
  if(!require("reshape")) {
    stop("Library reshape cannot be loaded. Check if it is installed. Stopping execution.")
  }
}

# Get traits
#get.traits <- function(sad) {
#
#  print("[debug] get.traits")
#
#  are.traits <- grep("Trait[.]Value", names(sad), value=T)

##   warning("Removing traits with no variation")
##   have.var <- function(x) length(unique(x))>1
##   are.var <- sapply(sad[are.traits], have.var)
##   are.traits <- are.traits[are.var]

#  are.traits
#}

# Get fixed effects
get.fixed <- function(sad) {

  print("[debug] get.fixed")

  are.levels <- grep("((Characteristics)|(Factor))", names(sad), value=T)
  #warning("Filtering factors to exclude *id* names -- only for Keygene data. Remove for other analyses!")
  are.levels <- grep("[Ii]d", are.levels, value=T, invert=T)
  are.var <- sapply(sad[are.levels], FUN = function(x) length(unique(x))>1 )
  are.fixed  <- grep("(Block)|(Field)|(Rank)|(Plot)|(Replic)|(Column)|(Row)|(Rand)", are.levels[are.var], value=T, invert=T)

  are.fixed
}

# Get random effects
get.random <- function(sad) {

  print("[debug] get.random")

  are.levels <- grep("((Characteristics)|(Factor))", names(sad), value=T)
  #warning("Filtering factors to exclude *id* names -- only for Keygene data. Remove for other analyses!")
  are.levels <- grep("[Ii]d", are.levels, value=T, invert=T)
  are.var <- sapply(sad[are.levels], FUN = function(x) length(unique(x))>1 )
  are.random <- grep("(Block)|(Field)|(Rank)|(Plot)|(Replic)|(Column)|(Row)|(Rand)", are.levels[are.var], value=T)

  are.random
}


# Save results to files
save.results <- function (sFile, aFile, experiment, means, models, elegant=TRUE) {

  print("[debug] save.results")

  sFile2 <- substr(sFile, start=0, stop=regexpr("[.]",sFile)-1)
  aFile2 <- substr(aFile, start=0, stop=regexpr("[.]",aFile)-1)

  #rFile <- paste(sFile2, aFile2, "obj.R", sep="_")
  #save(experiment, file=rFile)
  #save(models, file=rFile)
  #print(paste("R objects saved to file:", rFile))

  if (elegant) {
    drop <- grep(colnames(means), pattern="Formula")
    max <- length(colnames(means))

    rf <- "Factor.Value.Random."
    emax <- length(colnames(experiment))
    if (colnames(experiment)[emax] == rf) {
      col <- grep(colnames(means), pattern=rf)
      row <- grep(means[,col], pattern="[*]")
      rmax <- dim(means)[1]
      means <- rbind(means[1:(row-1),], means[(row+1):rmax,])
      means <- cbind(Parameter=means[,1:(drop-1)], means[,(drop+1):(col-1)], means[,(col+1):max])
    }
    else {
      means <- cbind(Parameter=means[,1:(drop-1)], means[,(drop+1):max])
    }
  }

  statFile <- paste(paste("data_model", sFile2, aFile2, sep="_"),".txt",sep="")
  write.table(means, file=paste("results/", statFile, sep=""), sep="\t", na="", row.names=F, quote=F)
  print(paste("Sufficient statistics saved to file: ", statFile))

  statFile
}

# Update assay file to include sufficient statistics column
update.aFile <- function(aFile, statFile) {

  print("[debug] update.aFile")

  print("Updating..")
  a <- read.table(aFile, header=T, check.names=F, sep="\t")
  a <- cbind(a, "Protocol REF"="data modelling", "Derived Data File"=statFile)

  if (length(grep(aFile, pat="results/")) == 0){
    aFile <- paste("results/", aFile, sep="")
  }
  write.table(a, na="", row.names=F, sep="\t", quote=F, file=aFile)
  print(paste("Assay file", aFile, "updated to include data modelling column"))

}

#Udpade inevstigation by adding info about a protocol for lme4
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
  inv[prot, prot.count+1] <- "data modelling"
  inv[grep(pat="Study Protocol Type$", inv), prot.count + 1] <- "data analysis"
  desc <- "Observations for all traits are analysed by estimation of linear mixed models, with use of lme4 package for R."
  inv[grep(pat="Study Protocol Description", inv), prot.count + 1] <- desc

  if (length(grep(ipath, pat="results/")) == 0){
    ipath <- paste("results/", ipath, sep="")
  }
  write.table(x=inv, ipath, na="", col.names=F, sep="\t", quote = F, row.names=F)
  print("Investigation file updated.")
}

# Zip files
zip.files <- function() {

  print("[debug] zip.sufficient.files")

  files.new <- list.files("results", full.names = T)
  files.old <- list.files(include.dirs = FALSE, pattern = "[^.zip]$")
  files.unchanged <- setdiff(files.old, list.files("results"))
  files <- c(files.unchanged, files.new)
  zip(zipfile="original.zip", files=files.old, flags = "-j")
  zip(zipfile="results.zip", files=files, flags = "-j")

}




# Run processing: find, read, model, save
run <- function() {

  print("[debug] run")

  saPairs <- get.isaFiles()

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

    traits <- names(d)[-1]

    sad.names <- c(sa.names, d.names)

    sad <- get.sad(sa, d)
    tmp <- check.random(sad, sad.names)
    sad <- tmp$sad
    sad.names <- tmp$names

    result <- get.models(sad, sad.names, traits)
    means <- result$means
    models <- result$models
    success <- result$success

    means <- change.names(means, sad.names)

    #statFile <- save.results(sFile, aFile, sad, means, models)
    #saPairs[i,5] <<- statFile
    # update isa-tab file to include sufficient data file
    #remFiles <- update.aFile(aFile, statFile, success == 0)
    #zip.files(remFiles)


    # update isa-tab file to include sufficient data file
    {
      results.exists <- length(grep(dir(), pat="^results$")) != 0
      if (!results.exists) {
        dir.create("results")
      }

      statFile <- save.results(sFile, aFile, sad, means, models)
      saPairs[i,5] <- statFile
      iFile <- saPairs[i,1]
      if (results.exists) {
        aFile <- paste("results/", aFile, sep="")
        iFile <- paste("results/", iFile, sep="")
      }
      update.aFile(aFile, statFile)   
      update.investigation(iFile)
      zip.files()
    }
    
  }
} 


# Things to do before running in Java

# remove global variables (<<-)
# uncomment:
 args <- commandArgs(TRUE)
 if (length(args) > 0) {
   setwd(args[1])
 }

options(stringsAsFactors=FALSE)
#setwd("C:/Users/hcwi/Dropbox/IGR/phenalyse/phen-stats/isatab - Kopia")
#setwd("C:/Users/hnk/Dropbox/IGR/phenalyse/phen-stats - new/isatab-new")
run()
