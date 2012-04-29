#!/usr/bin/python

from collections import namedtuple
from batch_runner import batch_run, plot_num_agents

if __name__ == "__main__":
    num_trials = 30
    num_steps = 800
    for disease in ['none', 'cold', 'malaria', 'avian-flu']:
        for flocking, flocking_factor in [('yes', 2.5), ('no', 0.0)]:
            for observability, observability_factor in [('perfect', 1.0), ('poor', 0.2)]:
                for paranoid, symptom_tolerance in [('yes', 0.2), ('no', 0.8)]:
                    sim_args = ("-for %d -disease %s -flocking %f -observability %f -symptom-tolerance %f" % (
                        num_steps, disease, flocking_factor, observability_factor, symptom_tolerance)).split()
                    title = "disease=%s flocking=%s observability=%s paranoid=%s" % (
                        disease, flocking, observability, paranoid)
                    fig_path = "plots/fig-disease=%s,flocking=%s,observability=%s,paranoid=%s.png" % (
                        disease, flocking, observability, paranoid)
                    print
                    print "*" * 78
                    print "Doing %s:" % title
                    stats = batch_run(num_trials, sim_args)
                    plot_num_agents(stats, title, fig_path)

    print "All done."
