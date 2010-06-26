#!/usr/bin/python

import sys
import cgi
import os
import string
import cgitb; cgitb.enable()

import examples

if sys.platform == 'darwin':
    os.environ['PATH'] = os.environ['PATH'] + ':/sw/bin:/usr/local/bin'

if os.environ.has_key('DISPLAY'):
    del os.environ['DISPLAY']

form = cgi.FieldStorage()

def pipedata(command, data):
    (cin, cout, cerr) = os.popen3(command)
    try:
        cin.write(data)
        success = True
    except:
        success = False
    cin.flush()
    cin.close()
    result = cout.read()
    cout.close()
    errs = cerr.read()
    cerr.close()
    if success:
        return (result, errs)
    else:
        sys.stderr.write('Stdout:\n' + result)
        sys.stderr.write('Stderr:\n' + errs)
        raise

def failrun(errors):
    print """Content-type: text/html

<html>
<head><title>Failed run</title></head>
<body>
<p>Stoj failed with error messages:</p>
<pre>%s</pre>
</body>
</html>""" % (cgi.escape(errors),)
    sys.exit()

def urlEncode(s):
    s = s.replace(' ', '%20')
    s = s.replace('\n', '%0A')
    s = s.replace('\r', '%0D')
    s = s.replace('\t', '%09')
    return s

def visprog(program):
    (graph, errors) = pipedata('./visdump', program)
    if errors: failrun(errors)
    print """Content-type: text/html

<html>
  <head>
      <title>StoJ Program Visualisation</title>
  </head>
  <body>
    <applet code="Vis.class" codebase="%(codebase)s" archive="vis/stojvis.jar" width=800 height=600>
      <param name=graph value="%(graph)s">
    </applet>
  </body>
</html>
""" % {'graph': urlEncode(graph),
       'codebase': ('//' + os.environ['HTTP_HOST'] + ':' + os.environ['SERVER_PORT'] +
                    os.environ['REQUEST_URI'])}

if form.has_key('program') and form.has_key('limit') and form.has_key('seed'):
    program = form['program'].value
    event_limit = 10000000
    row_limit = int(form['limit'].value)
    if row_limit > 1000000 or row_limit <= 0:
        raise 'Limit must be >0 and <1000000.', row_limit
    seed = int(form['seed'].value)
    if form.has_key('timelimit'):
        timelimit = float(form['timelimit'].value)
    else:
        timelimit = 0.0

    if form.has_key('action') and form['action'].value == 'Visualise program':
        visprog(program)
    else:
        (csv, errors) = pipedata('./stojrun --events %i --rows %i --seed %i' %
                                 (event_limit, row_limit, seed), program)
        if errors: failrun(errors)
        if form.has_key('csvmode'):
            print 'Content-type: text/plain'
            print
            sys.stdout.write(csv)
        else:
            (gnuplotcommands, errors) = pipedata('./graphconc %f' % (timelimit,), csv)
            if errors: failrun(errors)
            (png, errors) = pipedata('gnuplot', gnuplotcommands)
            if errors and not string.strip(errors).startswith('gnuplot:'): failrun(errors)
            print 'Content-type: image/png'
            print
            sys.stdout.write(png)
    sys.exit(0)

all_example_names = filter(lambda x: not x.startswith("_"), dir(examples))
all_example_names.sort()
all_examples = {}
for name in all_example_names:
    all_examples[name] = getattr(examples, name)

if form.has_key('example'):
    example_name = form['example'].value
else:
    example_name = all_example_names[0]
example = all_examples[example_name]

print """Content-type: text/html

<html>
<head>
  <title>StoJ evaluator</title>
</head>
<body>
<h1>StoJ evaluator</h1>
<form method="get">
Example: <select name="example">
""",

for name in all_example_names:
    if name == example_name:
        print '<option selected="yes">%s</option>' % name
    else:
        print '<option>%s</option>' % name

print """</select><input type="submit" value="Load example">
</form>
<form method="post">
<p>
Program text (see below for syntax):<br>
<textarea name="program" cols=80 rows=10>%(program)s</textarea><br>
</p>
<p>
<table border=0 cellspacing=5 cellpadding=0>
<tr>
  <td>Row limit:</td>
  <td><input name="limit" value="%(limit)d"></td>
  <td>Maximum number of output data points to allow the system to generate.</td>
</tr>
<tr>
  <td>RNG Seed:</td>
  <td><input name="seed" value="%(seed)d"></td>
  <td>Seed for the random number generator. Choose any integer.</td>
</tr>
<tr>
  <td>Graph time limit:</td>
  <td><input name="timelimit" value="%(timelimit)g"></td>
  <td>Restrict the X axis of the produced graph to the given time in seconds, or
  0 to display all generated data points.</td>
</tr>
<tr>
  <td>Produce CSV:</td>
  <td><input name="csvmode" type="checkbox"></td>
  <td>Check this box to produce a CSV record of the program output;
  leave the box cleared to produce a graph of program output instead.</td>
</tr>
</table>
</p>
<input type="submit" name="action" value="Submit program">&nbsp;
<input type="submit" name="action" value="Visualise program">
</form>
<pre>%(readme)s</pre>
</body>
</html>
""" % { 'program': open(example[0]).read(),
        'limit': example[1],
        'seed': example[2],
        'timelimit': example[3],
        'readme': open('README').read() }
