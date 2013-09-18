/*
	Copyright 2011-2012 TSB, http://www.tsbtecnologias.es
	TSB - Tecnologías para la Salud y el Bienestar
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universaal.nativeandroid.aalfficiencyclient.utils;

public class Scores {

	private int totalScore; 
	private int ElectricScore; 
	private int ActivityScore;
	private int todayScore; 
	private int ElectricTodayScore; 
	private int ActivityTodayScore;
	private double ElectricPercentage;
	private double ActivityPercentage;
	
	
	public int getTodayScore() {
		return todayScore;
	}
	public void setTodayScore(int todayScore) {
		this.todayScore = todayScore;
	}
	public int getElectricTodayScore() {
		return ElectricTodayScore;
	}
	public void setElectricTodayScore(int electricTodayScore) {
		ElectricTodayScore = electricTodayScore;
	}
	public int getActivityTodayScore() {
		return ActivityTodayScore;
	}
	public void setActivityTodayScore(int activityTodayScore) {
		ActivityTodayScore = activityTodayScore;
	}
	public int getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}
	public int getElectricScore() {
		return ElectricScore;
	}
	public void setElectricScore(int electricScore) {
		ElectricScore = electricScore;
	}
	public int getActivityScore() {
		return ActivityScore;
	}
	public void setActivityScore(int activityScore) {
		ActivityScore = activityScore;
	}
	public double getElectricPercentage() {
		return ElectricPercentage;
	}
	public void setElectricPercentage(double electricPercentage) {
		ElectricPercentage = electricPercentage;
	}
	public double getActivityPercentage() {
		return ActivityPercentage;
	}
	public void setActivityPercentage(double activityPercentage) {
		ActivityPercentage = activityPercentage;
	}
	
	
	
}
