#!/usr/bin/python

import json
import matplotlib.pyplot as plt
import numpy as np
import subprocess
import tempfile
import time

sim_exe = ["java", "DiseaseSpread"]


def extract_time_series(stats, num_trials, num_steps, key):
    "Helper: extracts a time series from JSON stats."
    result = np.empty((num_trials, num_steps), dtype=np.int)
    for trial, stat in enumerate(stats):
        result[trial] = stat[key]
    return result


def plot_num_agents(stats):
    """
    Plots number of total agents and number of sick agents, min, max, mean,
    and std. stats is a list of JSON stats from the simulation.
    """
    num_trials = len(stats)
    assert num_trials > 0
    num_steps = len(stats[0]['numAgentsAlive'])
    assert num_steps > 0
    agents_alive = extract_time_series(stats, num_trials, num_steps, 'numAgentsAlive')
    agents_infected = extract_time_series(stats, num_trials, num_steps, 'numAgentsInfected')
    fig = plt.figure()
    ax = fig.add_subplot(111)
    xs = np.arange(num_steps)
    ax.errorbar(xs, np.mean(agents_alive, axis=0),
                yerr=np.std(agents_alive, axis=0),
                color='green', label='alive')
    ax.errorbar(xs, np.mean(agents_infected, axis=0),
                yerr=np.std(agents_infected, axis=0),
                color='red', label='infected')
    ax.set_xlabel("simulation step")
    ax.set_ylabel("number of agents")
    ax.legend()
    plt.show()


def batch_run(num_trials, sim_args):
    """
    Runs the simulation num_trials times, with the given sim_args. Saves logs
    and reports aggregate statistics.
    """
    # Stats extracted from each run:
    stats = []
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
        # <json data>
        # Quit
        log.seek(0)
        for line in log:
            if line.startswith('======='):
                break
        log.next()
        data = json.loads(log.next())
        assert data
        stats.append(data)
        print "  -> extracted stats"

    # Show aggregate stats:
    assert len(stats) == num_trials
    plot_num_agents(stats)
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

