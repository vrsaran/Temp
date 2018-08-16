node {
	       def mvnHome
	   def projName = "${env.JOB_NAME}"
	def workName = "${env.WORKSPACE}"
	
	   def gitRepo = 'https://github.com/rekhapani/referenceImplementatoin.git'
	   stage('Preparation') { // for display purposes
	      // Get some code from a GitHub repository
	      git gitRepo
	      // Get the Maven tool.
	      // ** NOTE: This 'Maven' Maven tool must be configured
	      // **       in the global configuration.           
	      mvnHome = tool 'Maven'
	   }
	   stage('Build') {
	      // Run the maven build
	      
	bat(/"${mvnHome}\bin\mvn"  -f ${projName}\pom.xml clean install/)
	     
	}
	stage('SonarQube analysis') {
	    ws("${workName}\\${projName}") {
	    // requires SonarQube Scanner 2.8+
	    def scannerHome = tool 'SONARSCANNER';
	    withSonarQubeEnv('SONARQUBE 7.2') {
	      
	       bat(/"${mvnHome}\bin\mvn" org.codehaus.mojo:cobertura-maven-plugin:cobertura test  org.sonarsource.scanner.maven:sonar-maven-plugin:3.0.2:sonar -Dsonar.java.coveragePlugin=cobertura /) 
	      
	    }
	/*context="sonarqube/qualitygate"
	     
	        timeout(time: 2, unit: 'MINUTES') { // Just in case something goes wrong, pipeline will be killed after a timeout
	            def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
	            if (qg.status != 'OK') {
	             
	                error "Pipeline aborted due to quality gate failure: \${qg.status}"
	            } else {
	              
	            }    
	        }*/
	}
	}
	stage('Results') {
	      junit '**/target/surefire-reports/TEST-*.xml'
	      archive 'target/*.jar'
	      cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/target/site/cobertura/coverage.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: true, failUnstable: true, lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
	
	   }
	
	}
