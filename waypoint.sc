waypoints_file = read_file('waypoints','JSON');
saveSystem() -> (
    write_file('waypoints', 'JSON',global_waypoints);
);
if(waypoints_file == null, global_waypoints = m(l('Origin',l(0,100,0))); saveSystem();, global_waypoints = waypoints_file; );
list() -> print(global_waypoints);
del(name) -> {
    if(delete(global_waypoints,name),print('Waypoint ' + name + ' deleted.'), _error('Waypoint ' + name + ' does not exist'));
    saveSystem();
};

add(name, poi_pos, description) -> {
	if(has(global_waypoints, name)
    put(global_waypoints, name, poi_pos);
    saveSystem();
};

tp(name) -> {
    loc = global_waypoints:name:0;
    if(loc == null, _error('That waypoint does not exist'));
    print('Teleporting ' +player()+ ' to ' + name);
    run(('tp '+player()+ ' ' + loc:0 + ' ' + loc:1 + ' ' + loc:2));
    return();
};

_error(msg)->(
	print(player(), format(str('r %s', msg)));
	exit()
);

__command() -> '';
__config() -> {
    'scope'->'global',
   'commands' -> 
   {
      'del <waypoint>' -> 'del',
      'add <name>' -> ['add', null, null],
	  'add <name> <pos>' -> ['add', null],
	  'add <name> <description>' -> ['add', null],
      'tp <waypoint>' -> 'tp',

      'list' -> 'list'
   },
   'arguments' -> {
      'waypoint' -> {
            'type' -> 'term',
            'suggester'-> _(args) -> copy(keys(global_waypoints)),
      },
	  'name' -> {
			'type' -> 'term',
			'suggest' -> [], //to make it not suggest anything
	  },
	  'description' -> {
			'type' -> 'text',
			'suggest' -> [],
	  },
   }
};





