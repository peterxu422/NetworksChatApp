#http://www.cs.swarthmore.edu/~newhall/unixhelp/javamakefiles.html
#JFLAGS = -g
#JC = javac
#.SUFFIXES: .java .class
#.java.class:
#	$(JC) $(JFLAGS) $*.java

#CLASSES = \
	ClientListener.java \
	ClientSender.java \
	User.java \
	Sender.java \
	Client.java \
	ServerSender.java \
	ChatServer.java 

#default: classes

#classes: $(CLASSES:.java=.class)

#clean:
#	$(RM) *.class

JAVAC=javac
sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -rf *.class

%.class : %.java
	$(JAVAC) $<
