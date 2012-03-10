VERSION=0.0.1
PKGNAME=rainbow
BINDIR=bin
JAVAC=/usr/bin/javac
JAVALIB=/usr/share/java/
GSONJAR=/usr/share/java/google-gson-2.1/gson-2.1.jar
JUNITJAR=/usr/share/java/junit.jar
SRCFILES=`find ./src -name *.java`

all:
	@mkdir -p $(BINDIR);
	@$(JAVAC) -Xlint:unchecked -cp $(GSONJAR):$(JUNITJAR):$(JAVALIB) $(SRCFILES) -d $(BINDIR) 

install:
	@mkdir -p $(JAVALIB)/$(PKGNAME)
	@cp -rf $(BINDIR)/$(PKGNAME)/* $(JAVALIB)/$(PKGNAME)/

clean:
	@rm -rf $(BINDIR);
