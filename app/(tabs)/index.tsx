import { View, Text, StyleSheet, FlatList, Alert, TouchableOpacity, RefreshControl, Vibration, AppState, AppStateStatus } from 'react-native';
import { Theme } from '@/constants/Colors';
import FriendCard from '@/components/feature/FriendCard';
import Toast from '@/components/ui/Toast';
import { useEffect, useState, useCallback, useRef } from 'react';
import { useRouter } from 'expo-router';
import * as UsageStats from '@/modules/usage-stats';
import { useAuthStore } from '@/stores/authStore';
import { useFriendStore, FriendWithStatus } from '@/stores/friendStore';
import { supabase, Peep } from '@/lib/supabase';

// Helper function to get friendly app names
function getFriendlyAppName(packageName: string): string {
    const appMap: Record<string, string> = {
        'com.google.android.youtube': 'Watching YouTube 📺',
        'com.instagram.android': 'On Instagram 📸',
        'com.whatsapp': 'Chatting on WhatsApp 💬',
        'com.twitter.android': 'On Twitter/X 🐦',
        'com.facebook.katana': 'On Facebook 📘',
        'com.spotify.music': 'Listening to Spotify 🎵',
        'com.netflix.mediaclient': 'Watching Netflix 🎬',
        'com.snapchat.android': 'On Snapchat 👻',
        'com.tiktok': 'Scrolling TikTok 🎵',
        'com.google.android.gm': 'Checking Email 📧',
        'com.google.android.apps.maps': 'Using Maps 🗺️',
        'com.android.chrome': 'Browsing Chrome 🌐',
        'com.zhiliaoapp.musically': 'Scrolling TikTok 🎵',
        'com.anonymous.peep': 'Using Peep 👁️',
        'com.google.android.dialer': 'On a Call 📞',
        'com.android.contacts': 'Looking at Contacts 📇',
    };

    return appMap[packageName] || `Using ${packageName.split('.').pop()} 📱`;
}

export default function HomeScreen() {
    const router = useRouter();
    const { user, profile, signOut } = useAuthStore();
    const { friends, isLoading, fetchFriends, subscribeToStatusUpdates, unsubscribeFromStatusUpdates, getFriendStatus } = useFriendStore();

    const [refreshing, setRefreshing] = useState(false);
    const [peepingId, setPeepingId] = useState<string | null>(null);

    // Toast state
    const [toastVisible, setToastVisible] = useState(false);
    const [toastMessage, setToastMessage] = useState('');

    // Broadcasting state
    const broadcastInterval = useRef<NodeJS.Timeout | null>(null);
    const appState = useRef(AppState.currentState);

    const showToast = (message: string) => {
        setToastMessage(message);
        setToastVisible(true);
        Vibration.vibrate([0, 50, 30, 50]);
    };

    // Smart broadcast - only when app is active
    const broadcastMyStatus = async () => {
        if (!user) return;

        try {
            const currentApp = await UsageStats.getForegroundApp();
            if (currentApp) {
                const friendlyName = getFriendlyAppName(currentApp);
                await supabase.from('user_status').upsert({
                    user_id: user.id,
                    current_app: currentApp,
                    friendly_name: friendlyName,
                    updated_at: new Date().toISOString(),
                }, { onConflict: 'user_id' });
                console.log('Broadcasted status:', friendlyName);
            }
        } catch (error) {
            console.error('Broadcast error:', error);
        }
    };

    const startBroadcasting = (intervalMs: number = 15000) => {
        // Broadcast immediately
        broadcastMyStatus();

        // Then broadcast at specified interval
        if (broadcastInterval.current) {
            clearInterval(broadcastInterval.current);
        }
        broadcastInterval.current = setInterval(broadcastMyStatus, intervalMs);
        console.log(`Started broadcasting (every ${intervalMs / 1000}s)`);
    };

    const stopBroadcasting = () => {
        if (broadcastInterval.current) {
            clearInterval(broadcastInterval.current);
            broadcastInterval.current = null;
            console.log('Stopped broadcasting');
        }
    };

    // Initialize on mount
    useEffect(() => {
        if (user) {
            // Fetch friends first, THEN subscribe to status updates
            const initializeApp = async () => {
                await fetchFriends(user.id);
                // Now friends are loaded, subscribe to real-time status updates
                subscribeToStatusUpdates(user.id);
            };

            initializeApp();

            // Start broadcasting (every 15 seconds for real-time updates)
            startBroadcasting(15000);

            // Listen for app state changes (foreground/background)
            const subscription = AppState.addEventListener('change', (nextAppState: AppStateStatus) => {
                if (appState.current.match(/inactive|background/) && nextAppState === 'active') {
                    // App came to foreground - broadcast frequently (15 sec)
                    startBroadcasting(15000);
                    // Also refresh friend status when coming back to app
                    fetchFriends(user.id);
                } else if (nextAppState.match(/inactive|background/)) {
                    // App went to background - keep broadcasting but slower (30 sec) to save battery
                    startBroadcasting(30000);
                }
                appState.current = nextAppState;
            });

            // Subscribe to incoming peeps - show subtle toast notification
            const channel = supabase
                .channel('my-peeps')
                .on(
                    'postgres_changes',
                    {
                        event: 'INSERT',
                        schema: 'public',
                        table: 'peeps',
                        filter: `to_user_id=eq.${user.id}`,
                    },
                    async (payload) => {
                        const peep = payload.new as Peep;

                        // Get the peeper's profile
                        const { data: peeper } = await supabase
                            .from('profiles')
                            .select('username')
                            .eq('id', peep.from_user_id)
                            .single();

                        // Show subtle toast instead of alert
                        showToast(`👀 ${peeper?.username || 'Someone'} peeped you!`);
                    }
                )
                .subscribe();

            return () => {
                stopBroadcasting();
                subscription.remove();
                supabase.removeChannel(channel);
                unsubscribeFromStatusUpdates();
            };
        }
    }, [user]);

    const onRefresh = useCallback(async () => {
        if (!user) return;
        setRefreshing(true);
        await fetchFriends(user.id);
        setRefreshing(false);
    }, [user]);

    const handlePeep = async (friend: FriendWithStatus) => {
        if (!user) return;

        // Check permission first
        const hasPerm = await UsageStats.hasPermission();
        if (!hasPerm) {
            Alert.alert('Permission Needed', 'Allow usage access to peep friends.', [
                { text: 'Open Settings', onPress: UsageStats.requestPermission },
                { text: 'Cancel', style: 'cancel' },
            ]);
            return;
        }

        setPeepingId(friend.id);

        try {
            // Get friend's current status from Supabase
            const status = await getFriendStatus(friend.id);
            const friendlyName = status?.friendly_name || 'Offline 💤';

            // Record the peep in database
            await supabase.from('peeps').insert({
                from_user_id: user.id,
                to_user_id: friend.id,
                detected_app: status?.current_app || null,
                friendly_name: friendlyName,
            });

            // Send push notification to friend via Edge Function
            try {
                await supabase.functions.invoke('send-peep-notification', {
                    body: {
                        from_user_id: user.id,
                        to_user_id: friend.id,
                        friendly_name: friendlyName,
                    },
                });
            } catch (pushError) {
                console.log('Push notification failed (non-critical):', pushError);
            }

            // Show what friend is doing
            showToast(`${friend.username}: ${friendlyName}`);

            // Refresh friends list to update the card with latest status
            await fetchFriends(user.id);
        } catch (error) {
            console.error('Peep error:', error);
            showToast('Could not peep friend');
        } finally {
            setPeepingId(null);
        }
    };

    const handleAddFriend = () => {
        router.push('/friends');
    };

    return (
        <View style={styles.container}>
            {/* Toast Notification */}
            <Toast
                message={toastMessage}
                visible={toastVisible}
                onHide={() => setToastVisible(false)}
                duration={3000}
            />

            {/* BeReal-style centered header */}
            <View style={styles.header}>
                <Text style={styles.logo}>PeeP.</Text>
            </View>

            {/* Friends List */}
            {friends.length === 0 && !isLoading ? (
                <View style={styles.emptyState}>
                    <Text style={styles.emptyTitle}>Wow, it's really calm in here!</Text>
                    <Text style={styles.emptySubtitle}>
                        Your friends haven't posted yet.{'\n'}Be the first one.
                    </Text>
                    <TouchableOpacity style={styles.emptyButton} onPress={handleAddFriend}>
                        <Text style={styles.emptyButtonText}>Add Friends</Text>
                    </TouchableOpacity>
                </View>
            ) : (
                <FlatList
                    data={friends}
                    keyExtractor={item => item.id}
                    renderItem={({ item }) => (
                        <FriendCard
                            name={item.username}
                            status={item.status?.friendly_name || 'Tap to peep 👁️'}
                            peepsRemaining={99}
                            onPeep={() => handlePeep(item)}
                            isPeeping={peepingId === item.id}
                        />
                    )}
                    contentContainerStyle={styles.list}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor="#FFFFFF"
                        />
                    }
                />
            )}

            {/* Add Friend FAB */}
            <TouchableOpacity style={styles.fab} onPress={handleAddFriend} activeOpacity={0.8}>
                <Text style={styles.fabText}>+</Text>
            </TouchableOpacity>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#000000',
    },
    header: {
        paddingTop: 56,
        paddingBottom: 16,
        alignItems: 'center',
    },
    logo: {
        color: '#FFFFFF',
        fontSize: 26,
        fontWeight: '800',
        letterSpacing: 0.5,
    },
    list: {
        paddingHorizontal: 16,
        paddingBottom: 100,
    },
    emptyState: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 40,
    },
    emptyTitle: {
        color: '#FFFFFF',
        fontSize: 20,
        fontWeight: '700',
        marginBottom: 10,
        textAlign: 'center',
    },
    emptySubtitle: {
        color: '#999999',
        fontSize: 15,
        textAlign: 'center',
        lineHeight: 22,
        marginBottom: 28,
    },
    emptyButton: {
        backgroundColor: '#FFFFFF',
        paddingHorizontal: 28,
        paddingVertical: 14,
        borderRadius: 24,
    },
    emptyButtonText: {
        color: '#000000',
        fontSize: 15,
        fontWeight: '700',
    },
    fab: {
        position: 'absolute',
        bottom: 100,
        right: 20,
        width: 56,
        height: 56,
        borderRadius: 28,
        backgroundColor: '#FFFFFF',
        justifyContent: 'center',
        alignItems: 'center',
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 8,
    },
    fabText: {
        color: '#000000',
        fontSize: 28,
        fontWeight: '300',
        lineHeight: 30,
    },
});
