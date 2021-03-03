waypoints_file = read_file('waypoints','JSON');
saveSystem() -> (
    write_file('waypoints', 'JSON',global_waypoints);
);
global_authors = {};
if(waypoints_file == null, 
	global_waypoints = {'Origin' ->[[0,100,0], null, null]); saveSystem(), 
	global_waypoints = waypoints_file; 
	map(values(global_waypoints), if( (auth = _:2) != null, global_authors += auth))
);

list(author) -> (

);

del(name) -> (
    if(delete(global_waypoints,name),print('Waypoint ' + name + ' deleted.'), _error('Waypoint ' + name + ' does not exist'));
    saveSystem();
);

add(name, poi_pos, description) -> (
	if(has(global_waypoints, name), 
		_error('You are trying to overwrite an existing waypoint. Delete it first.'),
		// else, add new one
		player = player();
		if(poi_pos==null, poi_pos=player~'pos');
		global_waypoints:name = [poi_pos, description, player];
		global_authors += player;
		print(player, format(
			'g Added new waypoint ',
			str('bg %s ', name),
			str('g at %s %s %s', map(poi_pos, round(_))),
		));
		saveSystem();
	);
);

tp(name) -> (
    loc = global_waypoints:name:0;
    if(loc == null, _error('That waypoint does not exist'));
    print('Teleporting ' +player()+ ' to ' + name);
    run(('tp '+player()+ ' ' + loc:0 + ' ' + loc:1 + ' ' + loc:2));
    return();
);

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
	  'add <name> <pos> <description>' -> 'add',
      'tp <waypoint>' -> 'tp',
	  'list' -> ['list', null],
      'list <author>' -> 'list',
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
	  'author' -> {
            'type' -> 'term',
            'suggester'-> _(args) -> copy(keys(global_authors)),
      },
   }
};





