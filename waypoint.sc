waypoints_file = read_file('waypoints','JSON');
saveSystem() -> (
    write_file('waypoints', 'JSON',global_waypoints);
);
if(waypoints_file == null, global_waypoints = m(l('Origin',l(0,100,0))); saveSystem();, global_waypoints = waypoints_file; );
list() -> print(global_waypoints);
del(name) -> {
    if(delete(global_waypoints,name),print('Waypoint ' + name + ' deleted.'), print('Waypoint ' + name + 'does not exist'));
    saveSystem();
};

add(name, poi_pos) -> {
    put(global_waypoints, name, poi_pos);
    saveSystem();
};

tp(name) -> {
    loc = global_waypoints:name;
    if(loc == null,print('That waypoint does not exist'); return());
    print('Teleporting ' +player()+ ' to ' + name);
    run(('tp '+player()+ ' ' + loc:0 + ' ' + loc:1 + ' ' + loc:2));
    return();
};
__command() -> '';
__config() -> {
    'scope'->'global',
   'commands' -> 
   {
      'del <waypoint>' -> 'del',
      'add <term> <pos>' -> 'add',
      'tp <waypoint>' -> 'tp',

      'list' -> 'list'
   },
   'arguments' -> {
      'waypoint' -> {
            'type' -> 'term',
            'suggester'-> _(args) -> keys(global_waypoints),
      }    
   }
};





