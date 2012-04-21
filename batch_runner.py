#!/usr/bin/python

import numpy as np
import subprocess
import tempfile
import time

sim_exe = ["java", "DiseaseSpread"]


def batch_run(num_trials, sim_args):
    """
    Runs the simulation num_trials times, with the given sim_args. Saves logs
    and reports aggregate statistics.
    """
    # Stats extracted from each run:
    nums_agents_alive = []
    nums_agents_infected = []
    run_times = []
    total_time = time.time()

    # Run num_trials simulations:
    print >>sys.stderr, "Running simulation %d times with args: %s" % (
            num_trials, sim_args)
    temp_dir = tempfile.mkdtemp(prefix='batch-', dir='.')
    log_pat = temp_dir + "/run%02d.log"
    print "Temp dir: %s" % temp_dir
    for trial in range(num_trials):
        # Run the simulation:
        log = open(log_pat % trial, 'w+')
        print "trial %d:" % trial
        run_time = time.time()
        subprocess.call(sim_exe + sim_args, stdout=log, stderr=log)
        run_time = time.time() - run_time
        run_times.append(run_time)
        print "  -> done in %f seconds" % run_time

        # Collect stats from log. Format:
        # ==============================================================================
        # End-of-run statistics:
        # numAgentsAlive=15
        # numAgentsInfected=0
        # Quit
        log.seek(0)
        for line in log:
            if line.startswith('======='):
                break
        log.next()
        l = log.next().split('=')
        assert l[0] == 'numAgentsAlive'
        nums_agents_alive.append(int(l[1]))
        l = log.next().split('=')
        assert l[0] == 'numAgentsInfected'
        nums_agents_infected.append(int(l[1]))
        print "  -> extracted stats"

    # Show aggregate stats:
    assert len(nums_agents_alive) == num_trials
    assert len(nums_agents_infected) == num_trials
    assert len(run_times) == num_trials
    print
    print "Aggregate stats:"
    print "nums_agents_alive: min=%d max=%d mean=%f std=%f" % (
        np.min(nums_agents_alive), np.max(nums_agents_alive),
        np.mean(nums_agents_alive), np.std(nums_agents_alive))
    print "nums_agents_infected: min=%d max=%d mean=%f std=%f" % (
        np.min(nums_agents_infected), np.max(nums_agents_infected),
        np.mean(nums_agents_infected), np.std(nums_agents_infected))
    print "run_times: min=%f max=%f mean=%f std=%f" % (
        np.min(run_times), np.max(run_times),
        np.mean(run_times), np.std(run_times))

    total_time = time.time() - total_time
    print
    print "-> %d trials finished in %f seconds" % (num_trials, total_time)


if __name__ == "__main__":
    # Batch-run parameters:
    num_trials = None
    sim_args = None

    # Parse command-line parameters:
    import sys
    pos = 1
    while pos < len(sys.argv):
        if sys.argv[pos] == '--num':
            num_trials = int(sys.argv[pos + 1])
            pos += 2
        elif sys.argv[pos] == '--sim-args':
            sim_args = sys.argv[pos + 1].split()
            pos += 2
        else:
            print >>sys.stderr, "Bad argument '%s'." % sys.argv[pos]
            pos += 1
    if num_trials is None:
        raise Exception("--num required")
    if sim_args is None:
        raise Exception("--sim-args required")

    # Run it!
    batch_run(num_trials, sim_args)

