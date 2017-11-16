/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pgtrafpol.execution;

import java.util.List;

/**
 *
 * @author bdi
 */
public class Logic {

    public String id;
    public int n_phases;
    public int n_yellow_phases;
    public int n_red_green_phases;
    public List<String> phases;
    public List<Integer> phases_durations;
    public int max_offset_duration;
}