git add *.sh
git add src/Cliente/*.java
git add src/Servidor/Simple*.java
git add src/AlarmeCovidLN/*.java

if [ $# -eq 0 ]
	then
		git commit -m "cenas"
	else
		git commit -m "$1"
fi
git pull
git push
