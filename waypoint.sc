global_waypoint_config = {
    // Config option to allow players to tp to the waypoints ( Either via `/waypoint list` or `/waypoint tp` ) 
	// 0 : NEVER
	// 1 : CREATIVE PLAYERS
	// 2 : CREATIVE AND SPECTATOR PLAYERS
	// 3 : ALWAYS
    'allow_tp' -> 3
};

_can_player_tp() -> (
	global_waypoint_config:'allow_tp' == 3 || 
	( global_waypoint_config:'allow_tp' == 1 && player()~'gamemode'=='creative') ||
	( global_waypoint_config:'allow_tp' == 2 && player()~'gamemode_id'%2)
);
_is_tp_allowed() -> global_waypoint_config:'allow_tp'; // anything but 0 will give boolean true

waypoints_file = read_file('waypoints','JSON');
saveSystem() -> (
    write_file('waypoints', 'JSON',global_waypoints);
);
global_authors = {};
global_dimensions = {'overworld'}; // so we only show waypoints in dimensions that have any; shoud also support custom ones
if(waypoints_file == null, 
	global_waypoints = {'Origin' ->[[0,100,0], 'Default waypoint', null, 'overworld']}; saveSystem(),
	global_waypoints = waypoints_file; 
	map(values(global_waypoints), 
		if( (auth = _:2) != null, global_authors += auth);
		global_dimensions += _:3
	);
);

_get_list_item(name, data, tp_allowed) -> (
	desc = if(data:1, '^g ' + data:1);
	cond_desc = if(!tp_allowed, desc);
	item = ['by \ \ '+name , desc, str('w : %s %s %s ', map(data:0, round(_)))];
	if(tp_allowed, 
		item += str('!/%s tp %s', system_info('app_name'), name);
		item += '^g Click to teleport!',
		// if tp is not allowed, append description tooltip
		item += desc
	);
	if(data:2, 
		item += 'g by ';
		item += cond_desc;
		item += 'gb '+data:2;
		//if(!_is_tp_allowed(), item += desc)
		item += cond_desc;
	);
	item
);

list(author) -> (
	player = player();
	if(author != null && !has(global_authors, author), _error(author + ' has not set any waypoints'));
	print(player, format('bc === List of current waypoints ==='));
	tp_allowed = _can_player_tp();
	for(global_dimensions,
		current_dim = _;
		dim_already_printed = false;
		for(pairs(global_waypoints),
			[name, data]= _;
			if(current_dim== data:3 && (author == null || author==data:2),
				if(!dim_already_printed, print(player, format('l in '+current_dim)); dim_already_printed=true); // to avoid printing dim header when filtering authors
				print(player, format( _get_list_item(name, data, tp_allowed)))
			)
		)
	)
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
		global_waypoints:name = [poi_pos, description, str(player), player~'dimension'];
		global_authors += str(player);
		global_dimensions += player~'dimension';
		print(player, format(
			'g Added new waypoint ',
			str('bg %s ', name),
			str('g at %s %s %s', map(poi_pos, round(_))),
		));
		saveSystem();
	);
);

edit(name, description) -> (
	if(!has(global_waypoints, name), _error('That waypoint does not exist'));
	global_waypoints:name:1 = description;
	print(player(), format('g Edited waypoint\'s description'))
);

tp(name) -> (
    if(!_can_player_tp(), _error(str('%s players are not allowed to teleport', player()~'gamemode')) ); //for modes 1 and 2
    loc = global_waypoints:name:0;
	dim = global_waypoints:name:3;
    if(loc == null, _error('That waypoint does not exist'));
    print('Teleporting ' +player()+ ' to ' + name);
    run(str('execute in %s run tp %s %s %s %s', dim, player(), loc:0, loc:1, loc:2));
);

help() -> (
	player = player();
	print(player, format('by ==Help for the Waypoints app=='));
	print(player, format(str('g the following commands are available with /%s', system_info('app_name')) ));
	print(player, format('b \ \ add <name> [pos] [description]', 'w : add a new waypoint at given position with given description'));
	print(player, format('b \ \ del <waypoint>', 'w : delete existing waypoint'));
	print(player, format('b \ \ edit <waypoint> <description>', 'w : edit the description of an existing waypoint'));
	print(player, format('b \ \ list [author]', 'w : list all existing waypoints, optionally filtering by author'));
	if(_is_tp_allowed(),  print(player, format('b \ \ tp <waypoint>', 'w : teleport to given waypoint')));	
);

_error(msg)->(
	print(player(), format(str('r %s', msg)));
	exit()
);

_get_commands() -> (
    base_commands = {
	  '' -> 'help',
      'del <waypoint>' -> 'del',
      'add <name>' -> ['add', null, null],
	  'add <name> <pos>' -> ['add', null],
	  'add <name> <pos> <description>' -> 'add',
	  'edit <waypoint> <description>' -> 'edit',
	  'list' -> ['list', null],
      'list <author>' -> 'list',
   };
   if(_is_tp_allowed(), put(base_commands, 'tp <waypoint>', 'tp'));
   base_commands;
);

__config() -> {
    'scope'->'global',
	'stay_loaded'-> true,
    'commands' -> _get_commands(),
    'arguments' -> {
      'waypoint' -> {
            'type' -> 'term',
            'suggester'-> _(args) -> keys(global_waypoints),
      },
	  'name' -> {
			'type' -> 'term',
			'suggest' -> [], // to make it not suggest anything
	  },
	  'description' -> {
			'type' -> 'text',
			'suggest' -> [],
	  },
	  'author' -> {
            'type' -> 'term',
            'suggester'-> _(args) -> keys(global_authors),
      },
   }
   
};
