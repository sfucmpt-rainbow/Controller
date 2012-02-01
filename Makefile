VERSION=0.0.1
SRCDIR=src
BINDIR=bin
JAVAC=/usr/bin/javac
JAVALIB=/usr/share/java/lib
ERLOTPJAR=/usr/share/java/lib/OtpErlang.jar

all:
	@mkdir -p $(BINDIR);
	@cd $(SRCDIR);
	@$(JAVAC) -cp $(SRCDIR):$(ERLOTPJAR) $(SRCDIR)/*.java
	@cd ../;
	@mv $(SRCDIR)/*.class $(BINDIR)/;

install:
	@mkdir -p $(JAVALIB);
	@cp -rf $(BINDIR)/* $(JAVALIB);

clean:
	@rm -rf $(BINDIR);
